package com.example.domain.repository

import com.example.core.error.ResultWrapper
import com.example.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(): Flow<ResultWrapper<List<Expense>>>
    suspend fun getExpenseById(id: String): ResultWrapper<Expense>
    suspend fun insertExpense(expense: Expense): ResultWrapper<Unit>
    suspend fun deleteExpense(id: String): ResultWrapper<Unit>
    suspend fun clearAllExpenses(): ResultWrapper<Unit>
}
