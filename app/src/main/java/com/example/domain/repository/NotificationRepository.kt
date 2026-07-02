package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getProcessedNotificationTexts(): Flow<List<String>>
    suspend fun saveProcessedNotificationText(text: String)
}
