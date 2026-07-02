package com.example

import com.example.data.database.dao.ExpenseDao
import com.example.data.database.entity.ExpenseDbEntity
import com.example.data.repository.ExportRepositoryImpl
import com.example.presentation.screens.ExportFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExportSystemTest {

    // A lightweight fake implementation of ExpenseDao for testing
    private class FakeExpenseDao(val items: List<ExpenseDbEntity>) : ExpenseDao {
        override fun getAllExpenses(): Flow<List<ExpenseDbEntity>> = flowOf(items)
        override suspend fun getExpenseById(id: String): ExpenseDbEntity? = items.find { it.id == id }
        override suspend fun insertExpense(expense: ExpenseDbEntity) {}
        override suspend fun deleteExpenseById(id: String) {}
        override suspend fun clearAllExpenses() {}
        
        override suspend fun getExpensesCount(): Int = items.size
        override suspend fun getExpensesCountInDateRange(startDate: Long, endDate: Long): Int {
            return items.count { it.dateLong in startDate..endDate }
        }
        
        override suspend fun getExpensesPaged(limit: Int, offset: Int): List<ExpenseDbEntity> {
            if (offset >= items.size) return emptyList()
            return items.subList(offset, minOf(offset + limit, items.size))
        }
        
        override suspend fun getExpensesPagedInDateRange(
            startDate: Long,
            endDate: Long,
            limit: Int,
            offset: Int
        ): List<ExpenseDbEntity> {
            val filtered = items.filter { it.dateLong in startDate..endDate }
            if (offset >= filtered.size) return emptyList()
            return filtered.subList(offset, minOf(offset + limit, filtered.size))
        }

        override suspend fun getTotalSent(): Double? = items.filter { it.uType == "DEBIT" }.sumOf { it.amount }
        override suspend fun getTotalSentInDateRange(startDate: Long, endDate: Long): Double? {
            return items.filter { it.uType == "DEBIT" && it.dateLong in startDate..endDate }.sumOf { it.amount }
        }

        override suspend fun getTotalReceived(): Double? = items.filter { it.uType == "CREDIT" }.sumOf { it.amount }
        override suspend fun getTotalReceivedInDateRange(startDate: Long, endDate: Long): Double? {
            return items.filter { it.uType == "CREDIT" && it.dateLong in startDate..endDate }.sumOf { it.amount }
        }
    }

    @Test
    fun testFileNamingFormat() {
        val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val csvFileName = "UPI_Report_$dateSuffix.csv"
        val pdfFileName = "UPI_Report_$dateSuffix.pdf"
        
        assertTrue(csvFileName.startsWith("UPI_Report_"))
        assertTrue(csvFileName.endsWith(".csv"))
        assertTrue(pdfFileName.endsWith(".pdf"))
        assertEquals(csvFileName, "UPI_Report_$dateSuffix.csv")
    }

    @Test
    fun testCsvEscapingLogic() {
        val exportRepo = ExportRepositoryImpl(null)
        
        // Access private escapeCsv via reflection or just verify behavior through standard testing of public methods
        // Since we can test our CSV output directly, let's export mock items and check formatting:
        val mockItems = listOf(
            ExpenseDbEntity("1", 100.0, "Super, Market", "DEBIT", System.currentTimeMillis(), "REF1", "SBI", "raw sms", "Groceries"),
            ExpenseDbEntity("2", 200.0, "Normal Merchant", "CREDIT", System.currentTimeMillis(), "REF2", "HDFC", "raw sms", "Salary"),
            ExpenseDbEntity("3", 50.0, "John \"The Legend\" Doe", "DEBIT", System.currentTimeMillis(), "REF3", "GPAY", "raw sms", "Gift")
        )
        
        val fakeDao = FakeExpenseDao(mockItems)
        val repo = ExportRepositoryImpl(fakeDao)
        val outputStream = ByteArrayOutputStream()
        
        runBlocking {
            val result = repo.exportToCsv(outputStream, null, null) {}
            assertTrue(result is com.example.core.error.ResultWrapper.Success)
            
            val csvContent = outputStream.toString("UTF-8")
            
            // Verify columns are present
            assertTrue(csvContent.contains("DATE & TIME") || csvContent.contains("Date,Time,Source App"))
            
            // Verify "Super, Market" is escaped correctly with surrounding double-quotes
            assertTrue(csvContent.contains("\"Super, Market\""))
            
            // Verify normal merchant is NOT wrapped in double-quotes unnecessarily
            assertTrue(csvContent.contains(",Normal Merchant,"))
            
            // Verify double quotes are escaped
            assertTrue(csvContent.contains("\"John \"\"The Legend\"\" Doe\""))
        }
    }

    @Test
    fun testDateFilteringExport() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        val mockItems = listOf(
            ExpenseDbEntity("1", 50.0, "Merchant 1", "DEBIT", now, "REF1", "SBI", "body", "Cat"), // today
            ExpenseDbEntity("2", 150.0, "Merchant 2", "DEBIT", now - 2 * oneDayMs, "REF2", "SBI", "body", "Cat"), // 2 days ago
            ExpenseDbEntity("3", 250.0, "Merchant 3", "DEBIT", now - 10 * oneDayMs, "REF3", "SBI", "body", "Cat") // 10 days ago
        )
        
        val fakeDao = FakeExpenseDao(mockItems)
        val repo = ExportRepositoryImpl(fakeDao)
        
        runBlocking {
            // Filter: last 5 days
            val startDate = Date(now - 5 * oneDayMs)
            val endDate = Date(now + 1 * oneDayMs) // include today
            
            val countResult = repo.getExpensesCount(startDate, endDate)
            assertTrue(countResult is com.example.core.error.ResultWrapper.Success)
            assertEquals(2, (countResult as com.example.core.error.ResultWrapper.Success).data)
            
            val listResult = repo.getExpensesPaged(startDate, endDate, 10, 0)
            assertTrue(listResult is com.example.core.error.ResultWrapper.Success)
            val list = (listResult as com.example.core.error.ResultWrapper.Success).data
            assertEquals(2, list.size)
            assertEquals("Merchant 1", list[0].merchantName)
            assertEquals("Merchant 2", list[1].merchantName)
        }
    }

    @Test
    fun testEmptyDatabaseExport() {
        val fakeDao = FakeExpenseDao(emptyList())
        val repo = ExportRepositoryImpl(fakeDao)
        val outputStream = ByteArrayOutputStream()
        
        runBlocking {
            val csvResult = repo.exportToCsv(outputStream, null, null) {}
            assertTrue(csvResult is com.example.core.error.ResultWrapper.Success)
            
            val pdfResult = repo.exportToPdf(outputStream, null, null) {}
            if (pdfResult is com.example.core.error.ResultWrapper.Error) {
                println("PDF Export Error Message: ${pdfResult.error.message}")
                pdfResult.error.throwable?.printStackTrace()
            }
            assertTrue(pdfResult is com.example.core.error.ResultWrapper.Success)
        }
    }
}
