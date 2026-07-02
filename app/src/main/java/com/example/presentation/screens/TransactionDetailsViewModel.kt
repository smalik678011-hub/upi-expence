package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.domain.model.Expense
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransactionDetailsUiState {
    object Loading : TransactionDetailsUiState()
    data class Success(val expense: Expense) : TransactionDetailsUiState()
    data class Error(val message: String) : TransactionDetailsUiState()
}

class TransactionDetailsViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionDetailsUiState>(TransactionDetailsUiState.Loading)
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: String) {
        _uiState.value = TransactionDetailsUiState.Loading
        viewModelScope.launch {
            when (val result = expenseRepository.getExpenseById(id)) {
                is ResultWrapper.Success -> {
                    _uiState.value = TransactionDetailsUiState.Success(result.data)
                }
                is ResultWrapper.Error -> {
                    _uiState.value = TransactionDetailsUiState.Error(result.error.message ?: "Failed to load transaction")
                }
            }
        }
    }

    fun updateCategory(expense: Expense, newCategory: String) {
        val updated = expense.copy(category = newCategory)
        viewModelScope.launch {
            expenseRepository.insertExpense(updated)
            _uiState.value = TransactionDetailsUiState.Success(updated)
        }
    }

    fun updateStatus(expense: Expense, newStatus: String) {
        val updated = expense.copy(status = newStatus)
        viewModelScope.launch {
            expenseRepository.insertExpense(updated)
            _uiState.value = TransactionDetailsUiState.Success(updated)
        }
    }
}
