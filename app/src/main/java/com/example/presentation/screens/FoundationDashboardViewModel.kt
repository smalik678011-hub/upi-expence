package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.log.Logger
import com.example.core.security.SecureStorage
import com.example.core.utils.DispatcherProvider
import com.example.domain.repository.ExpenseRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.collect

class FoundationDashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationRepository: NotificationRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _statusMessage = MutableStateFlow("Initializing...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _smsTrackingEnabled = MutableStateFlow(true)
    val smsTrackingEnabled: StateFlow<Boolean> = _smsTrackingEnabled.asStateFlow()

    private val _dailyLimit = MutableStateFlow(5000.0)
    val dailyLimit: StateFlow<Double> = _dailyLimit.asStateFlow()

    private val _testRupeeAmount = MutableStateFlow(1234567.89)
    val testRupeeAmount: StateFlow<Double> = _testRupeeAmount.asStateFlow()

    private val _notificationListenerEnabled = MutableStateFlow(false)
    val notificationListenerEnabled: StateFlow<Boolean> = _notificationListenerEnabled.asStateFlow()

    private val _capturedNotifications = MutableStateFlow<List<String>>(emptyList())
    val capturedNotifications: StateFlow<List<String>> = _capturedNotifications.asStateFlow()

    init {
        verifyArchitecture()
        observeCapturedNotifications()
    }

    private fun observeCapturedNotifications() {
        viewModelScope.launch(dispatcherProvider.io) {
            notificationRepository.getProcessedNotificationTexts().collect { list ->
                _capturedNotifications.value = list
            }
        }
    }

    fun checkNotificationPermission(context: android.content.Context) {
        val enabled = com.example.core.utils.NotificationPermissionHelper.isNotificationListenerEnabled(context)
        _notificationListenerEnabled.value = enabled
        logger.d("DashboardVM", "Checked notification listener permission: $enabled")
    }

    private fun verifyArchitecture() {
        viewModelScope.launch(dispatcherProvider.io) {
            logger.i("DashboardVM", "Verifying DI and system configurations...")
            
            // Check SecureStorage
            secureStorage.putBoolean("di_test_verified", true)
            val secureStorageOk = secureStorage.getBoolean("di_test_verified", false)
            
            // Check settings repo
            val smsTracking = settingsRepository.isSmsTrackingEnabled()
            _smsTrackingEnabled.value = smsTracking
            
            val limit = settingsRepository.getDailyExpenseLimit()
            _dailyLimit.value = limit

            _statusMessage.value = if (secureStorageOk) {
                "Architecture verified and active."
            } else {
                "Module initialization completed with warnings."
            }
        }
    }

    fun toggleSmsTracking() {
        viewModelScope.launch(dispatcherProvider.io) {
            val next = !smsTrackingEnabled.value
            settingsRepository.setSmsTrackingEnabled(next)
            _smsTrackingEnabled.value = next
            logger.d("DashboardVM", "Toggled SMS tracking: $next")
        }
    }

    fun updateDailyLimit(limit: Double) {
        viewModelScope.launch(dispatcherProvider.io) {
            settingsRepository.setDailyExpenseLimit(limit)
            _dailyLimit.value = limit
            logger.d("DashboardVM", "Updated daily limit: $limit")
        }
    }
    
    fun setTestRupeeAmount(amount: Double) {
        _testRupeeAmount.value = amount
    }
}
