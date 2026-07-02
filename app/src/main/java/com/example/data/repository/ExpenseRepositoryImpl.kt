package com.example.data.repository

import com.example.core.error.ErrorModel
import com.example.core.error.ResultWrapper
import com.example.data.database.dao.ExpenseDao
import com.example.data.database.entity.ExpenseDbEntity
import com.example.domain.model.Expense
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.Date

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao? = null
) : ExpenseRepository {

    // Fallback in-memory list if DAO is not provided (e.g., in unit tests)
    private val _fallbackExpenses = MutableStateFlow<List<Expense>>(emptyList())

    override fun getExpenses(): Flow<ResultWrapper<List<Expense>>> {
        val dao = expenseDao
        return if (dao != null) {
            dao.getAllExpenses()
                .map { entities ->
                    val domainList = entities.map { it.toDomain() }
                    ResultWrapper.Success(domainList) as ResultWrapper<List<Expense>>
                }
                .catch { e ->
                    emit(ResultWrapper.Error(ErrorModel.DatabaseError.copy(throwable = e)))
                }
        } else {
            _fallbackExpenses.map { ResultWrapper.Success(it) }
        }
    }

    override suspend fun getExpenseById(id: String): ResultWrapper<Expense> {
        val dao = expenseDao
        return if (dao != null) {
            try {
                val entity = dao.getExpenseById(id)
                if (entity != null) {
                    ResultWrapper.Success(entity.toDomain())
                } else {
                    ResultWrapper.Error(ErrorModel.DatabaseError.copy(message = "Transaction not found"))
                }
            } catch (e: Exception) {
                ResultWrapper.Error(ErrorModel.DatabaseError.copy(throwable = e))
            }
        } else {
            val fallback = _fallbackExpenses.value.find { it.id == id }
            if (fallback != null) {
                ResultWrapper.Success(fallback)
            } else {
                ResultWrapper.Error(ErrorModel.DatabaseError.copy(message = "Transaction not found"))
            }
        }
    }

    override suspend fun insertExpense(expense: Expense): ResultWrapper<Unit> {
        val dao = expenseDao
        return if (dao != null) {
            try {
                dao.insertExpense(expense.toEntity())
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(ErrorModel.DatabaseError.copy(throwable = e))
            }
        } else {
            val current = _fallbackExpenses.value.toMutableList()
            current.add(0, expense)
            _fallbackExpenses.value = current
            ResultWrapper.Success(Unit)
        }
    }

    override suspend fun deleteExpense(id: String): ResultWrapper<Unit> {
        val dao = expenseDao
        return if (dao != null) {
            try {
                dao.deleteExpenseById(id)
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(ErrorModel.DatabaseError.copy(throwable = e))
            }
        } else {
            val current = _fallbackExpenses.value.toMutableList()
            current.removeAll { it.id == id }
            _fallbackExpenses.value = current
            ResultWrapper.Success(Unit)
        }
    }

    override suspend fun clearAllExpenses(): ResultWrapper<Unit> {
        val dao = expenseDao
        return if (dao != null) {
            try {
                dao.clearAllExpenses()
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.Error(ErrorModel.DatabaseError.copy(throwable = e))
            }
        } else {
            _fallbackExpenses.value = emptyList()
            ResultWrapper.Success(Unit)
        }
    }

    private fun ExpenseDbEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            merchantName = merchantName,
            uType = uType,
            date = Date(dateLong),
            transactionRef = transactionRef,
            accountOrBank = accountOrBank,
            rawSmsBody = rawSmsBody,
            category = category,
            status = status
        )
    }

    private fun Expense.toEntity(): ExpenseDbEntity {
        return ExpenseDbEntity(
            id = id,
            amount = amount,
            merchantName = merchantName,
            uType = uType,
            dateLong = date.time,
            transactionRef = transactionRef,
            accountOrBank = accountOrBank,
            rawSmsBody = rawSmsBody,
            category = category,
            status = status
        )
    }
}
