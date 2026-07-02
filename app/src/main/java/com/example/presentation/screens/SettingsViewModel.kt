package com.example.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.domain.model.Expense
import com.example.domain.repository.AppPreferencesRepository
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val isDeveloperModeEnabled: StateFlow<Boolean> = appPreferencesRepository.isDeveloperModeEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Data Management States
    private val _totalTransactions = MutableStateFlow(0)
    val totalTransactions: StateFlow<Int> = _totalTransactions.asStateFlow()

    private val _totalSentAmount = MutableStateFlow(0.0)
    val totalSentAmount: StateFlow<Double> = _totalSentAmount.asStateFlow()

    private val _totalReceivedAmount = MutableStateFlow(0.0)
    val totalReceivedAmount: StateFlow<Double> = _totalReceivedAmount.asStateFlow()

    private val _databaseSizeKb = MutableStateFlow(0.0)
    val databaseSizeKb: StateFlow<Double> = _databaseSizeKb.asStateFlow()

    private val _databaseVersion = MutableStateFlow(1)
    val databaseVersion: StateFlow<Int> = _databaseVersion.asStateFlow()

    // Temp cache for undo clear expenses
    private var deletedExpensesBackup: List<Expense>? = null

    init {
        // Collect expenses to compute statistics in real-time
        viewModelScope.launch {
            expenseRepository.getExpenses().collectLatest { result ->
                if (result is ResultWrapper.Success) {
                    val list = result.data
                    _totalTransactions.value = list.size
                    _totalSentAmount.value = list.filter { it.uType == "DEBIT" }.sumOf { it.amount }
                    _totalReceivedAmount.value = list.filter { it.uType == "CREDIT" }.sumOf { it.amount }
                }
            }
        }
    }

    fun updateDatabaseStats(context: Context) {
        viewModelScope.launch {
            try {
                val dbFile = context.getDatabasePath("upi_expenses_db")
                if (dbFile.exists()) {
                    _databaseSizeKb.value = dbFile.length() / 1024.0
                } else {
                    _databaseSizeKb.value = 0.0
                }
            } catch (e: Exception) {
                _databaseSizeKb.value = 0.0
            }
        }
    }

    fun clearAllTransactions(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Backup for undo
            val result = expenseRepository.getExpenses().stateIn(viewModelScope).value
            if (result is ResultWrapper.Success) {
                deletedExpensesBackup = result.data
            }
            
            val clearResult = expenseRepository.clearAllExpenses()
            if (clearResult is ResultWrapper.Success) {
                onSuccess()
            }
        }
    }

    fun undoClearTransactions(onSuccess: () -> Unit) {
        val backup = deletedExpensesBackup
        if (backup != null) {
            viewModelScope.launch {
                backup.forEach { expense ->
                    expenseRepository.insertExpense(expense)
                }
                deletedExpensesBackup = null
                onSuccess()
            }
        }
    }

    fun deleteAllDataAndReset(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Clear preferences
            appPreferencesRepository.clearAllPreferences()
            
            // Clear Database
            expenseRepository.clearAllExpenses()
            
            // Delete database files
            try {
                context.deleteDatabase("upi_expenses_db")
            } catch (e: Exception) {
                // Ignore
            }
            
            onSuccess()
        }
    }

    // Version click easter egg for Developer mode
    private var versionClickCount = 0

    fun onVersionClicked(onDeveloperModeUnlocked: () -> Unit) {
        if (isDeveloperModeEnabled.value) return
        versionClickCount++
        if (versionClickCount >= 7) {
            viewModelScope.launch {
                appPreferencesRepository.setDeveloperModeEnabled(true)
                onDeveloperModeUnlocked()
            }
            versionClickCount = 0
        }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setDeveloperModeEnabled(enabled)
        }
    }
}
