package com.example.presentation.screens

import com.example.domain.model.Expense

sealed interface HomeUiState {
    object Loading : HomeUiState
    
    object Empty : HomeUiState
    
    data class Success(
        val expenses: List<Expense>,
        val totalSent: Double,
        val totalReceived: Double,
        val netBalance: Double
    ) : HomeUiState
    
    data class Error(val message: String) : HomeUiState
}
