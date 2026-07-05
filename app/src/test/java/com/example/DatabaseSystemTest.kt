package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.dao.ExpenseDao
import com.example.data.database.entity.ExpenseDbEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseSystemTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = db.expenseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndFetchExpense() = runBlocking {
        val entity = ExpenseDbEntity(
            id = "test_1",
            amount = 150.0,
            merchantName = "Chai Shop",
            uType = "DEBIT",
            dateLong = System.currentTimeMillis(),
            transactionRef = "ref_123456",
            accountOrBank = "Google Pay",
            rawSmsBody = "Paid Rs 150 to Chai Shop",
            category = "Food",
            status = "SUCCESS"
        )

        expenseDao.insertExpense(entity)

        val retrieved = expenseDao.getExpenseById("test_1")
        assertNotNull(retrieved)
        assertEquals(150.0, retrieved!!.amount, 0.001)
        assertEquals("Chai Shop", retrieved.merchantName)
        assertEquals("Food", retrieved.category)
    }

    @Test
    fun testDuplicatePrevention_OnConflictReplace() = runBlocking {
        val entity1 = ExpenseDbEntity(
            id = "test_dup",
            amount = 150.0,
            merchantName = "Chai Shop",
            uType = "DEBIT",
            dateLong = System.currentTimeMillis(),
            transactionRef = "ref_dup",
            accountOrBank = "Google Pay",
            rawSmsBody = "Paid Rs 150 to Chai Shop",
            category = "Food"
        )

        val entity2 = ExpenseDbEntity(
            id = "test_dup",
            amount = 180.0, // Modified amount
            merchantName = "Chai Shop",
            uType = "DEBIT",
            dateLong = System.currentTimeMillis(),
            transactionRef = "ref_dup",
            accountOrBank = "Google Pay",
            rawSmsBody = "Paid Rs 180 to Chai Shop",
            category = "Food"
        )

        expenseDao.insertExpense(entity1)
        expenseDao.insertExpense(entity2) // Shoud REPLACE the existing entity due to matching PrimaryKey 'id'

        val count = expenseDao.getExpensesCount()
        assertEquals(1, count)

        val retrieved = expenseDao.getExpenseById("test_dup")
        assertEquals(180.0, retrieved!!.amount, 0.001)
    }

    @Test
    fun testExpensesSortingOrder() = runBlocking {
        val now = System.currentTimeMillis()
        val e1 = ExpenseDbEntity("1", 10.0, "M1", "DEBIT", now - 10000, "r1", "GPay", "", "Food")
        val e2 = ExpenseDbEntity("2", 20.0, "M2", "DEBIT", now, "r2", "GPay", "", "Food") // More recent
        val e3 = ExpenseDbEntity("3", 30.0, "M3", "DEBIT", now - 20000, "r3", "GPay", "", "Food")

        expenseDao.insertExpense(e1)
        expenseDao.insertExpense(e2)
        expenseDao.insertExpense(e3)

        val allExpenses = expenseDao.getAllExpenses().first()
        assertEquals(3, allExpenses.size)
        // Should be ordered by dateLong DESC (e2 -> e1 -> e3)
        assertEquals("2", allExpenses[0].id)
        assertEquals("1", allExpenses[1].id)
        assertEquals("3", allExpenses[2].id)
    }

    @Test
    fun testAggregateSums() = runBlocking {
        val now = System.currentTimeMillis()
        val debit1 = ExpenseDbEntity("1", 100.0, "Debit 1", "DEBIT", now, "r1", "GPay", "", "Food")
        val debit2 = ExpenseDbEntity("2", 150.0, "Debit 2", "DEBIT", now, "r2", "GPay", "", "Food")
        val credit1 = ExpenseDbEntity("3", 300.0, "Credit 1", "CREDIT", now, "r3", "GPay", "", "Salary")

        expenseDao.insertExpense(debit1)
        expenseDao.insertExpense(debit2)
        expenseDao.insertExpense(credit1)

        val totalSent = expenseDao.getTotalSent() ?: 0.0
        val totalReceived = expenseDao.getTotalReceived() ?: 0.0

        assertEquals(250.0, totalSent, 0.001)
        assertEquals(300.0, totalReceived, 0.001)
    }
}
