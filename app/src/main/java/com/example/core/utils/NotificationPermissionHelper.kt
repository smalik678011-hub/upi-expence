package com.example.core.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.core.notification.NotificationListenerService

object NotificationPermissionHelper {

    /**
     * Checks if the NotificationListenerService is granted permission in the system settings.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Creates an intent to open the Notification Listener Settings page.
     */
    fun getIntentForNotificationListenerSettings(): Intent {
        return Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
