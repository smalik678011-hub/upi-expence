package com.example.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    /**
     * Checks if battery optimization is disabled/ignored for this app.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * Returns an intent to prompt the user to ignore battery optimization.
     */
    fun getIntentForBatteryOptimizationSettings(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Provides standard manual guide instructions for major Indian market OEM devices.
     */
    fun getOemBatteryGuidance(brandName: String): String {
        return when (brandName.lowercase().trim()) {
            "samsung" -> "• Go to Settings → Apps → UPI Expense Tracker.\n• Tap 'Battery' and select 'Unrestricted'.\n• Ensure the app is NOT listed in 'Sleeping apps'."
            "xiaomi", "redmi", "poco" -> "• Go to Settings → Apps → Manage Apps → UPI Expense Tracker.\n• Tap 'Battery Saver' and select 'No restrictions'.\n• Turn ON the 'Autostart' permission toggle."
            "realme", "oppo" -> "• Go to Settings → Apps → App Management → UPI Expense Tracker.\n• Tap 'Battery Usage' and enable 'Allow background activity' and 'Allow auto-launch'."
            "vivo", "iqoo" -> "• Go to Settings → Battery → Background Power Consumption Management.\n• Select 'UPI Expense Tracker' and set to 'Don't restrict background power consumption'."
            "oneplus" -> "• Go to Settings → Apps → App Management → UPI Expense Tracker.\n• Tap 'Battery usage' and enable 'Allow background activity' and 'Allow auto-launch'."
            "motorola" -> "• Go to Settings → Apps → UPI Expense Tracker → Battery.\n• Select 'Unrestricted' background usage."
            "nothing" -> "• Go to Settings → Apps → All Apps → UPI Expense Tracker.\n• Tap 'Battery' and select 'Unrestricted'."
            "google", "pixel" -> "• Go to Settings → Apps → All Apps → UPI Expense Tracker.\n• Tap 'App battery usage' and select 'Unrestricted'."
            else -> "• Go to Settings → Apps → UPI Expense Tracker.\n• Locate 'Battery Optimization' or 'Background limits'.\n• Select 'Unrestricted' or 'Do not optimize' to allow continuous offline transaction processing."
        }
    }

    val supportedOems = listOf(
        "Google Pixel",
        "Samsung",
        "Xiaomi",
        "Realme",
        "Oppo",
        "Vivo",
        "OnePlus",
        "Motorola",
        "Nothing"
    )
}
