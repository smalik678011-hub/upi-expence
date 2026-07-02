package com.example.core.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle
import com.example.UpiExpenseApplication
import com.example.domain.model.NotificationData
import com.example.domain.repository.NotificationDispatcher
import com.example.domain.repository.NotificationValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListenerService : NotificationListenerService() {

    private lateinit var validator: NotificationValidator
    private lateinit var dispatcher: NotificationDispatcher
    private lateinit var notificationLogger: NotificationLogger
    private lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        
        try {
            val app = application as UpiExpenseApplication
            val container = app.container
            validator = container.notificationValidator
            dispatcher = container.notificationDispatcher
            notificationLogger = container.notificationLogger
            
            appScope = CoroutineScope(container.dispatcherProvider.io + SupervisorJob())
            notificationLogger.logError("NotificationListenerService successfully created and initialized.")
        } catch (e: Exception) {
            android.util.Log.e("NotificationListener", "Failed to initialize NotificationListenerService", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        try {
            val packageName = sbn.packageName ?: return
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: Bundle()

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val postTime = sbn.postTime
            val id = sbn.id
            val key = sbn.key
            val tag = sbn.tag

            val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
            val isSilent = isSilentNotification(sbn)

            if (!validator.validate(packageName, title, text, isOngoing, isSilent)) {
                notificationLogger.logRejected(packageName, id, "Failed validation (ongoing=$isOngoing, silent=$isSilent, textIsEmpty=${text.isNullOrBlank()})")
                return
            }

            val extrasMap = mutableMapOf<String, String>()
            for (keyStr in extras.keySet()) {
                try {
                    val value = extras.get(keyStr)
                    if (value != null) {
                        extrasMap[keyStr] = value.toString()
                    }
                } catch (e: Exception) {
                    // Ignore individual extra extraction failures
                }
            }

            val data = NotificationData(
                packageName = packageName,
                title = title,
                text = text,
                bigText = bigText,
                postTime = postTime,
                notificationId = id,
                notificationKey = key,
                tag = tag,
                extras = extrasMap
            )

            notificationLogger.logCaptured(data)

            appScope.launch {
                try {
                    dispatcher.dispatch(data)
                } catch (e: Exception) {
                    notificationLogger.logError("Failed to dispatch notification from ${data.source.displayName}", e)
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("NotificationListener", "Error processing notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    private fun isSilentNotification(sbn: StatusBarNotification): Boolean {
        val priority = sbn.notification.priority
        val isLowPriority = priority == Notification.PRIORITY_MIN || priority == Notification.PRIORITY_LOW
        
        val category = sbn.notification.category
        val isSystemSilentCategory = category == Notification.CATEGORY_STATUS || category == Notification.CATEGORY_SERVICE
        
        return isLowPriority || isSystemSilentCategory
    }
}
