package com.example.data.repository

import com.example.domain.model.NotificationData
import com.example.domain.repository.NotificationFilter
import com.example.domain.repository.NotificationValidator

class NotificationValidatorImpl(
    private val notificationFilter: NotificationFilter
) : NotificationValidator {

    override fun validate(
        packageName: String,
        title: String?,
        text: String?,
        isOngoing: Boolean,
        isSilent: Boolean
    ): Boolean {
        if (text.isNullOrBlank()) return false
        if (!notificationFilter.isSupportedPackage(packageName)) return false
        if (isOngoing) return false
        if (isSilent) return false
        if (title.isNullOrBlank()) return false
        return true
    }

    override fun validate(notificationData: NotificationData, isOngoing: Boolean, isSilent: Boolean): Boolean {
        return validate(
            packageName = notificationData.packageName,
            title = notificationData.title,
            text = notificationData.text,
            isOngoing = isOngoing,
            isSilent = isSilent
        )
    }
}
