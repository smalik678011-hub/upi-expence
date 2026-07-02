package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val isFirstLaunch: Flow<Boolean>
    val isPermissionTutorialShown: Flow<Boolean>
    val darkThemePreference: Flow<Boolean?>
    val languagePreference: Flow<String>
    val isDeveloperModeEnabled: Flow<Boolean>
    val isDynamicColorEnabled: Flow<Boolean>

    suspend fun setFirstLaunchCompleted(completed: Boolean)
    suspend fun setPermissionTutorialShown(shown: Boolean)
    suspend fun setDarkThemePreference(enabled: Boolean?)
    suspend fun setLanguagePreference(language: String)
    suspend fun setDeveloperModeEnabled(enabled: Boolean)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun clearAllPreferences()
}
