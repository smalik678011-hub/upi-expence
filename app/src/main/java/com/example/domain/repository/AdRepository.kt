package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface AdRepository {
    val isPremiumUser: Flow<Boolean>
    val isAdPersonalizationEnabled: Flow<Boolean>
    val interstitialTriggerCount: Flow<Int>

    suspend fun setPremiumUser(isPremium: Boolean)
    suspend fun setAdPersonalizationEnabled(enabled: Boolean)
    suspend fun incrementInterstitialTriggerCount()
    suspend fun resetInterstitialTriggerCount()
}
