package com.example.data.repository

import com.example.domain.model.NotificationSource
import com.example.domain.repository.NotificationFilter
import java.util.concurrent.ConcurrentHashMap

class NotificationFilterImpl : NotificationFilter {
    private val supportedPackages = ConcurrentHashMap.newKeySet<String>().apply {
        add(NotificationSource.GOOGLE_PAY.packageName)
        add(NotificationSource.PHONEPE.packageName)
        add(NotificationSource.PAYTM.packageName)
        add(NotificationSource.NAVI.packageName)
    }

    override fun isSupportedPackage(packageName: String): Boolean {
        return supportedPackages.contains(packageName)
    }

    override fun getSupportedPackages(): Set<String> {
        return supportedPackages.toSet()
    }

    override fun addSupportedPackage(packageName: String) {
        supportedPackages.add(packageName)
    }

    override fun removeSupportedPackage(packageName: String) {
        supportedPackages.remove(packageName)
    }
}
