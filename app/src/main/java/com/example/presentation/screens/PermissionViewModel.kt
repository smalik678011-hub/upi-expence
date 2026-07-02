package com.example.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.BatteryOptimizationHelper
import com.example.core.utils.NotificationPermissionHelper
import com.example.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PermissionUiState(
    val isNotificationListenerGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isPermissionTutorialShown: Boolean = false,
    val oemBrandGuidance: String = "",
    val selectedOemBrand: String = ""
)

class PermissionViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepository.isPermissionTutorialShown.collect { shown ->
                _uiState.update { it.copy(isPermissionTutorialShown = shown) }
            }
        }
    }

    fun checkPermissions(context: Context) {
        val notificationGranted = NotificationPermissionHelper.isNotificationListenerEnabled(context)
        val batteryIgnored = BatteryOptimizationHelper.isBatteryOptimizationIgnored(context)
        _uiState.update {
            it.copy(
                isNotificationListenerGranted = notificationGranted,
                isBatteryOptimizationIgnored = batteryIgnored
            )
        }
    }

    fun setTutorialShown() {
        viewModelScope.launch {
            appPreferencesRepository.setPermissionTutorialShown(true)
        }
    }

    fun selectOemBrand(brand: String) {
        val guidance = BatteryOptimizationHelper.getOemBatteryGuidance(brand)
        _uiState.update {
            it.copy(
                selectedOemBrand = brand,
                oemBrandGuidance = guidance
            )
        }
    }
}
