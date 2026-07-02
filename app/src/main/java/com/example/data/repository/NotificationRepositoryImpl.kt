package com.example.data.repository

import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepositoryImpl : NotificationRepository {
    private val _notifications = MutableStateFlow<List<String>>(emptyList())

    override fun getProcessedNotificationTexts(): Flow<List<String>> {
        return _notifications.asStateFlow()
    }

    override suspend fun saveProcessedNotificationText(text: String) {
        val current = _notifications.value.toMutableList()
        current.add(0, text)
        _notifications.value = current
    }
}
