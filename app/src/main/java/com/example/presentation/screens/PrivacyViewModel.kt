package com.example.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.core.utils.NotificationPermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrivacyViewModel : ViewModel() {

    private val _isNotificationAccessGranted = MutableStateFlow(false)
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    fun refreshPermissionStatus(context: Context) {
        _isNotificationAccessGranted.value = NotificationPermissionHelper.isNotificationListenerEnabled(context)
    }
}
