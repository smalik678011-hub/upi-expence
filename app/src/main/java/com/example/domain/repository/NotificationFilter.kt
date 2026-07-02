package com.example.domain.repository

interface NotificationFilter {
    fun isSupportedPackage(packageName: String): Boolean
    fun getSupportedPackages(): Set<String>
    fun addSupportedPackage(packageName: String)
    fun removeSupportedPackage(packageName: String)
}
