package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.core.log.Logger
import com.example.core.utils.DispatcherProvider
import com.example.domain.model.Expense
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger
) : ViewModel() {

    // Main UI State derived directly from Repository flow
    val uiState: StateFlow<HomeUiState> = expenseRepository.getExpenses()
        .map { result ->
            when (result) {
                is ResultWrapper.Success -> {
                    val list = result.data
                    if (list.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        var sent = 0.0
                        var received = 0.0
                        list.forEach { expense: Expense ->
                            if (expense.uType.equals("DEBIT", ignoreCase = true)) {
                                sent += expense.amount
                            } else if (expense.uType.equals("CREDIT", ignoreCase = true)) {
                                received += expense.amount
                            }
                        }
                        HomeUiState.Success(
                            expenses = list,
                            totalSent = sent,
                            totalReceived = received,
                            netBalance = received - sent
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    val errorMsg = result.error.message ?: "Failed to load transactions."
                    logger.e("HomeViewModel", "Error fetching expenses", result.error.throwable)
                    HomeUiState.Error(errorMsg)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun deleteTransaction(id: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            logger.i("HomeViewModel", "Deleting transaction ID: $id")
            val result = expenseRepository.deleteExpense(id)
            if (result is ResultWrapper.Error) {
                logger.e("HomeViewModel", "Failed to delete transaction $id", result.error.throwable)
            }
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch(dispatcherProvider.io) {
            logger.i("HomeViewModel", "Clearing all transactions")
            val result = expenseRepository.clearAllExpenses()
            if (result is ResultWrapper.Error) {
                logger.e("HomeViewModel", "Failed to clear transactions", result.error.throwable)
            }
        }
    }

    fun insertMockExpense(expense: Expense) {
        viewModelScope.launch(dispatcherProvider.io) {
            logger.i("HomeViewModel", "Inserting sandbox transaction: ${expense.merchantName}")
            expenseRepository.insertExpense(expense)
        }
    }
}
