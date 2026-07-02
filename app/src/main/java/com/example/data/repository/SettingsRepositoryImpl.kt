package com.example.data.repository

import com.example.core.security.SecureStorage
import com.example.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val secureStorage: SecureStorage
) : SettingsRepository {

    override suspend fun isSmsTrackingEnabled(): Boolean {
        return secureStorage.getBoolean(KEY_SMS_TRACKING, true)
    }

    override suspend fun setSmsTrackingEnabled(enabled: Boolean) {
        secureStorage.putBoolean(KEY_SMS_TRACKING, enabled)
    }

    override suspend fun getDailyExpenseLimit(): Double {
        val limitStr = secureStorage.getString(KEY_DAILY_LIMIT, "5000.0")
        return limitStr?.toDoubleOrNull() ?: 5000.0
    }

    override suspend fun setDailyExpenseLimit(limit: Double) {
        secureStorage.putString(KEY_DAILY_LIMIT, limit.toString())
    }

    companion object {
        private const val KEY_SMS_TRACKING = "pref_sms_tracking"
        private const val KEY_DAILY_LIMIT = "pref_daily_limit"
    }
}
