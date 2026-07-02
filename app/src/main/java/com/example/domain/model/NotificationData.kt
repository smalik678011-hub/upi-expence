package com.example.domain.model

data class NotificationData(
    val packageName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postTime: Long,
    val notificationId: Int,
    val notificationKey: String?,
    val tag: String?,
    val extras: Map<String, String>,
    val source: NotificationSource = NotificationSource.fromPackageName(packageName)
)
