package com.example.core.notification

import com.example.core.log.Logger
import com.example.domain.model.NotificationData

class NotificationLogger(private val logger: Logger) {
    fun logCaptured(notificationData: NotificationData) {
        val msg = "Captured notification: source=${notificationData.source.displayName}, package=${notificationData.packageName}, id=${notificationData.notificationId}, title=${notificationData.title}"
        logger.i("NotificationCapture", msg)
        com.example.core.log.InMemoryLogStore.addNotificationLog("CAPTURED: $msg")
        com.example.core.log.InMemoryLogStore.addRawNotification(notificationData)
    }

    fun logRejected(packageName: String, id: Int, reason: String) {
        val msg = "Rejected notification from pkg=$packageName, id=$id. Reason: $reason"
        logger.w("NotificationCapture", msg)
        com.example.core.log.InMemoryLogStore.addNotificationLog("REJECTED: $msg")
    }

    fun logError(message: String, throwable: Throwable? = null) {
        val msg = "Error: $message${throwable?.let { " - ${it.message}" } ?: ""}"
        logger.e("NotificationCapture", msg, throwable)
        com.example.core.log.InMemoryLogStore.addNotificationLog("ERROR: $msg")
    }
}
