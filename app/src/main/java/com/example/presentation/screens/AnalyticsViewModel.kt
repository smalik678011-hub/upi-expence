package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.domain.model.AnalyticsData
import com.example.domain.model.AnalyticsFilters
import com.example.domain.repository.AnalyticsRepository
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: AnalyticsData? = null,
    val filters: AnalyticsFilters = AnalyticsFilters(),
    val availableSourceApps: List<String> = emptyList()
)

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
        loadAvailableApps()
    }

    fun loadAnalytics() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = analyticsRepository.getAnalytics(_uiState.value.filters)
            when (result) {
                is ResultWrapper.Success -> {
                    _uiState.update { it.copy(isLoading = false, data = result.data, error = null) }
                }
                is ResultWrapper.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
            }
        }
    }

    private fun loadAvailableApps() {
        viewModelScope.launch {
            // Fetch all expenses to find unique source apps dynamically
            expenseRepository.getExpenses().collect { wrapper ->
                if (wrapper is ResultWrapper.Success) {
                    val apps = wrapper.data
                        .mapNotNull { it.accountOrBank }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    
                    // Always include core apps as default options if empty
                    val defaultApps = listOf("Google Pay", "PhonePe", "Paytm", "Navi")
                    val combinedApps = (defaultApps + apps).distinct().sorted()

                    _uiState.update { it.copy(availableSourceApps = combinedApps) }
                }
            }
        }
    }

    fun updateFilters(
        startDate: Date? = _uiState.value.filters.startDate,
        endDate: Date? = _uiState.value.filters.endDate,
        sourceApp: String? = _uiState.value.filters.sourceApp,
        transactionType: String? = _uiState.value.filters.transactionType,
        minAmount: Double? = _uiState.value.filters.minAmount,
        maxAmount: Double? = _uiState.value.filters.maxAmount
    ) {
        val newFilters = AnalyticsFilters(
            startDate = startDate,
            endDate = endDate,
            sourceApp = sourceApp,
            transactionType = transactionType,
            minAmount = minAmount,
            maxAmount = maxAmount
        )
        _uiState.update { it.copy(filters = newFilters) }
        loadAnalytics()
    }

    fun clearFilters() {
        _uiState.update { it.copy(filters = AnalyticsFilters()) }
        loadAnalytics()
    }
}
