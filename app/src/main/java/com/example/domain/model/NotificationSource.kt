package com.example.domain.model

enum class NotificationSource(val packageName: String, val displayName: String) {
    GOOGLE_PAY("com.google.android.apps.nbu.paisa.user", "Google Pay"),
    PHONEPE("com.phonepe.app", "PhonePe"),
    PAYTM("net.one97.paytm", "Paytm"),
    NAVI("com.naviapp", "Navi"),
    AMAZON_PAY("in.amazon.mShop.android.shopping", "Amazon Pay"),
    WHATSAPP_PAY("com.whatsapp", "WhatsApp Pay"),
    BHIM("in.org.npci.upiapp", "BHIM"),
    CRED("com.dreamplug.androidapp", "CRED"),
    SUPER_MONEY("com.supermoney", "super.money"),
    OTHER("", "Other");

    companion object {
        fun fromPackageName(packageName: String): NotificationSource {
            return values().find { it.packageName == packageName } ?: OTHER
        }
    }
}
