package com.example.core.log

import com.example.domain.model.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InMemoryLogStore {
    private val _parserLogs = MutableStateFlow<List<String>>(emptyList())
    val parserLogs: StateFlow<List<String>> = _parserLogs.asStateFlow()

    private val _notificationLogs = MutableStateFlow<List<String>>(emptyList())
    val notificationLogs: StateFlow<List<String>> = _notificationLogs.asStateFlow()

    private val _rawNotifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val rawNotifications: StateFlow<List<NotificationData>> = _rawNotifications.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun addParserLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val log = "[$timestamp] $message"
        _parserLogs.value = (listOf(log) + _parserLogs.value).take(100)
    }

    fun addNotificationLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val log = "[$timestamp] $message"
        _notificationLogs.value = (listOf(log) + _notificationLogs.value).take(100)
    }

    fun addRawNotification(notification: NotificationData) {
        _rawNotifications.value = (listOf(notification) + _rawNotifications.value).take(50)
    }

    fun clearAllLogs() {
        _parserLogs.value = emptyList()
        _notificationLogs.value = emptyList()
        _rawNotifications.value = emptyList()
    }
}
