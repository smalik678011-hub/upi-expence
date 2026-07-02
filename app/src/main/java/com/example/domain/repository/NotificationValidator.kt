package com.example.domain.repository

import com.example.domain.model.NotificationData

interface NotificationValidator {
    fun validate(packageName: String, title: String?, text: String?, isOngoing: Boolean, isSilent: Boolean): Boolean
    fun validate(notificationData: NotificationData, isOngoing: Boolean, isSilent: Boolean): Boolean
}
