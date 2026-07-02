package com.example.domain.repository

import com.example.domain.model.NotificationData

interface NotificationDispatcher {
    suspend fun dispatch(notificationData: NotificationData)
}
