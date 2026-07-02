package com.example.domain.repository

interface SettingsRepository {
    suspend fun isSmsTrackingEnabled(): Boolean
    suspend fun setSmsTrackingEnabled(enabled: Boolean)
    suspend fun getDailyExpenseLimit(): Double
    suspend fun setDailyExpenseLimit(limit: Double)
}
