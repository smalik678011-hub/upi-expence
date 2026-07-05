package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.AdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.adDataStore: DataStore<Preferences> by preferencesDataStore(name = "upi_tracker_ads")

class AdRepositoryImpl(private val context: Context) : AdRepository {

    private object PreferencesKeys {
        val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEY_AD_PERSONALIZATION = booleanPreferencesKey("ad_personalization")
        val KEY_INTERSTITIAL_COUNT = intPreferencesKey("interstitial_count")
    }

    override val isPremiumUser: Flow<Boolean> = context.adDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] ?: false
        }

    override val isAdPersonalizationEnabled: Flow<Boolean> = context.adDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_AD_PERSONALIZATION] ?: true
        }

    override val interstitialTriggerCount: Flow<Int> = context.adDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_INTERSTITIAL_COUNT] ?: 0
        }

    override suspend fun setPremiumUser(isPremium: Boolean) {
        context.adDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = isPremium
        }
    }

    override suspend fun setAdPersonalizationEnabled(enabled: Boolean) {
        context.adDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_AD_PERSONALIZATION] = enabled
        }
    }

    override suspend fun incrementInterstitialTriggerCount() {
        context.adDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_INTERSTITIAL_COUNT] ?: 0
            preferences[PreferencesKeys.KEY_INTERSTITIAL_COUNT] = current + 1
        }
    }

    override suspend fun resetInterstitialTriggerCount() {
        context.adDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_INTERSTITIAL_COUNT] = 0
        }
    }
}
