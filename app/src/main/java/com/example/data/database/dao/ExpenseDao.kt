package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.ExpenseDbEntity
import kotlinx.coroutines.flow.Flow

data class ExpenseAnalyticsProjection(
    val amount: Double,
    val uType: String,
    val dateLong: Long,
    val accountOrBank: String?,
    val merchantName: String
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateLong DESC")
    fun getAllExpenses(): Flow<List<ExpenseDbEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseDbEntity?

    @Query("SELECT amount, uType, dateLong, accountOrBank, merchantName FROM expenses WHERE status = 'SUCCESS'")
    suspend fun getAnalyticsProjection(): List<ExpenseAnalyticsProjection>

    @Query("""
        SELECT amount, uType, dateLong, accountOrBank, merchantName 
        FROM expenses 
        WHERE (dateLong >= :startDate OR :startDate = 0)
          AND (dateLong <= :endDate OR :endDate = 0)
          AND (accountOrBank = :sourceApp OR :sourceApp IS NULL)
          AND (uType = :uType OR :uType IS NULL)
          AND (amount >= :minAmount OR :minAmount = 0.0)
          AND (amount <= :maxAmount OR :maxAmount = 0.0)
          AND status = 'SUCCESS'
    """)
    suspend fun getAnalyticsProjectionFiltered(
        startDate: Long,
        endDate: Long,
        sourceApp: String?,
        uType: String?,
        minAmount: Double,
        maxAmount: Double
    ): List<ExpenseAnalyticsProjection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseDbEntity)


    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpensesCount(): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE dateLong >= :startDate AND dateLong <= :endDate")
    suspend fun getExpensesCountInDateRange(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM expenses ORDER BY dateLong DESC LIMIT :limit OFFSET :offset")
    suspend fun getExpensesPaged(limit: Int, offset: Int): List<ExpenseDbEntity>

    @Query("SELECT * FROM expenses WHERE dateLong >= :startDate AND dateLong <= :endDate ORDER BY dateLong DESC LIMIT :limit OFFSET :offset")
    suspend fun getExpensesPagedInDateRange(startDate: Long, endDate: Long, limit: Int, offset: Int): List<ExpenseDbEntity>

    @Query("SELECT SUM(amount) FROM expenses WHERE uType = 'DEBIT'")
    suspend fun getTotalSent(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE uType = 'DEBIT' AND dateLong >= :startDate AND dateLong <= :endDate")
    suspend fun getTotalSentInDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE uType = 'CREDIT'")
    suspend fun getTotalReceived(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE uType = 'CREDIT' AND dateLong >= :startDate AND dateLong <= :endDate")
    suspend fun getTotalReceivedInDateRange(startDate: Long, endDate: Long): Double?
}
