package com.example

import com.example.data.database.dao.ExpenseAnalyticsProjection
import com.example.data.database.dao.ExpenseDao
import com.example.data.database.entity.ExpenseDbEntity
import com.example.data.repository.AnalyticsRepositoryImpl
import com.example.domain.model.AnalyticsFilters
import com.example.domain.model.InsightType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AnalyticsSystemTest {

    private class FakeExpenseDao(val items: List<ExpenseDbEntity>) : ExpenseDao {
        override fun getAllExpenses(): Flow<List<ExpenseDbEntity>> = flowOf(items)
        override suspend fun getExpenseById(id: String): ExpenseDbEntity? = items.find { it.id == id }
        override suspend fun insertExpense(expense: ExpenseDbEntity) {}
        override suspend fun deleteExpenseById(id: String) {}
        override suspend fun clearAllExpenses() {}
        override suspend fun getExpensesCount(): Int = items.size
        override suspend fun getExpensesCountInDateRange(startDate: Long, endDate: Long): Int = 0
        override suspend fun getExpensesPaged(limit: Int, offset: Int): List<ExpenseDbEntity> = emptyList()
        override suspend fun getExpensesPagedInDateRange(s: Long, e: Long, l: Int, o: Int): List<ExpenseDbEntity> = emptyList()
        override suspend fun getTotalSent(): Double? = 0.0
        override suspend fun getTotalSentInDateRange(s: Long, e: Long): Double? = 0.0
        override suspend fun getTotalReceived(): Double? = 0.0
        override suspend fun getTotalReceivedInDateRange(s: Long, e: Long): Double? = 0.0

        override suspend fun getAnalyticsProjection(): List<ExpenseAnalyticsProjection> {
            return items.map {
                ExpenseAnalyticsProjection(
                    amount = it.amount,
                    uType = it.uType,
                    dateLong = it.dateLong,
                    accountOrBank = it.accountOrBank,
                    merchantName = it.merchantName ?: ""
                )
            }
        }

        override suspend fun getAnalyticsProjectionFiltered(
            startDate: Long,
            endDate: Long,
            sourceApp: String?,
            uType: String?,
            minAmount: Double,
            maxAmount: Double
        ): List<ExpenseAnalyticsProjection> {
            return items.filter {
                val matchDate = (startDate == 0L || it.dateLong >= startDate) && (endDate == 0L || it.dateLong <= endDate)
                val matchApp = sourceApp == null || it.accountOrBank == sourceApp
                val matchType = uType == null || it.uType == uType
                val matchAmount = (minAmount == 0.0 || it.amount >= minAmount) && (maxAmount == 0.0 || it.amount <= maxAmount)
                matchDate && matchApp && matchType && matchAmount && it.status == "SUCCESS"
            }.map {
                ExpenseAnalyticsProjection(
                    amount = it.amount,
                    uType = it.uType,
                    dateLong = it.dateLong,
                    accountOrBank = it.accountOrBank,
                    merchantName = it.merchantName ?: ""
                )
            }
        }
    }

    @Test
    fun testEmptyAnalyticsData() {
        val fakeDao = FakeExpenseDao(emptyList())
        val repository = AnalyticsRepositoryImpl(fakeDao)

        runBlocking {
            val result = repository.getAnalytics(AnalyticsFilters())
            assertTrue(result is com.example.core.error.ResultWrapper.Success)
            val data = (result as com.example.core.error.ResultWrapper.Success).data
            assertEquals(0, data.totalTransactions)
            assertEquals(0.0, data.lifetimeSpent, 0.0)
            assertEquals(0.0, data.lifetimeReceived, 0.0)
            assertTrue(data.insights.isEmpty())
        }
    }

    @Test
    fun testKpiAndInsightsCalculation() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        val mockItems = listOf(
            // Debits (Spent)
            ExpenseDbEntity("1", 500.0, "Amazon", "DEBIT", now, "TX1", "Google Pay", "raw sms 1", "Groceries", "SUCCESS"),
            ExpenseDbEntity("2", 1500.0, "Flipkart", "DEBIT", now - oneDayMs, "TX2", "PhonePe", "raw sms 2", "Shopping", "SUCCESS"),
            ExpenseDbEntity("3", 200.0, "Amazon", "DEBIT", now - 2 * oneDayMs, "TX3", "Google Pay", "raw sms 3", "Groceries", "SUCCESS"),
            
            // Credits (Received)
            ExpenseDbEntity("4", 5000.0, "Salary Account", "CREDIT", now, "TX4", "Paytm", "raw sms 4", "Salary", "SUCCESS"),
            ExpenseDbEntity("5", 1000.0, "Friend transfer", "CREDIT", now - 10 * oneDayMs, "TX5", "Paytm", "raw sms 5", "Gift", "SUCCESS")
        )

        val fakeDao = FakeExpenseDao(mockItems)
        val repository = AnalyticsRepositoryImpl(fakeDao)

        runBlocking {
            val result = repository.getAnalytics(AnalyticsFilters())
            assertTrue(result is com.example.core.error.ResultWrapper.Success)
            val data = (result as com.example.core.error.ResultWrapper.Success).data

            // Verify KPI Calculations
            assertEquals(5, data.totalTransactions)
            assertEquals(2200.0, data.lifetimeSpent, 0.0)
            assertEquals(6000.0, data.lifetimeReceived, 0.0)
            assertEquals(3800.0, data.netBalance, 0.0)
            assertEquals(1500.0, data.highestSingleExpense, 0.0)
            assertEquals(5000.0, data.highestSingleCredit, 0.0)

            // Verify Insights
            assertFalse(data.insights.isEmpty())
            
            // Most used app should be Paytm or Google Pay (Paytm has 2 count, Google Pay has 2 count)
            val topAppInsight = data.insights.find { it.title == "Most Used UPI App" }
            assertNotNull(topAppInsight)
            assertTrue(topAppInsight!!.value == "Google Pay" || topAppInsight.value == "Paytm")

            // Top connection should be Amazon (most frequent merchant)
            val connectionInsight = data.insights.find { it.title == "Top Connection" }
            assertNotNull(connectionInsight)
            assertEquals("Amazon", connectionInsight!!.value)
        }
    }

    @Test
    fun testFilteringLogic() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        val mockItems = listOf(
            ExpenseDbEntity("1", 500.0, "Amazon", "DEBIT", now, "TX1", "Google Pay", "raw sms", "Groceries", "SUCCESS"),
            ExpenseDbEntity("2", 1500.0, "Flipkart", "DEBIT", now - oneDayMs, "TX2", "PhonePe", "raw sms", "Shopping", "SUCCESS"),
            ExpenseDbEntity("3", 2000.0, "Friend", "CREDIT", now, "TX3", "Paytm", "raw sms", "Gift", "SUCCESS")
        )

        val fakeDao = FakeExpenseDao(mockItems)
        val repository = AnalyticsRepositoryImpl(fakeDao)

        runBlocking {
            // Filter by Google Pay only
            val filterAppResult = repository.getAnalytics(AnalyticsFilters(sourceApp = "Google Pay"))
            val appData = (filterAppResult as com.example.core.error.ResultWrapper.Success).data
            assertEquals(1, appData.totalTransactions)
            assertEquals(500.0, appData.lifetimeSpent, 0.0)

            // Filter by DEBIT only
            val filterTypeResult = repository.getAnalytics(AnalyticsFilters(transactionType = "DEBIT"))
            val typeData = (filterTypeResult as com.example.core.error.ResultWrapper.Success).data
            assertEquals(2, typeData.totalTransactions)
            assertEquals(2000.0, typeData.lifetimeSpent, 0.0)

            // Filter by Amount Range: 1000 to 2000
            val filterAmountResult = repository.getAnalytics(AnalyticsFilters(minAmount = 1000.0, maxAmount = 2000.0))
            val amountData = (filterAmountResult as com.example.core.error.ResultWrapper.Success).data
            assertEquals(2, amountData.totalTransactions) // Flipkart (1500 DEBIT) and Friend (2000 CREDIT)
        }
    }
}
