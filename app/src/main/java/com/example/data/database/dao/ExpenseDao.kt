package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.ExpenseDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateLong DESC")
    fun getAllExpenses(): Flow<List<ExpenseDbEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseDbEntity?

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
