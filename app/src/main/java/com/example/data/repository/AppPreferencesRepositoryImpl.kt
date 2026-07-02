package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upi_tracker_settings")

class AppPreferencesRepositoryImpl(private val context: Context) : AppPreferencesRepository {

    private object PreferencesKeys {
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_IS_PERMISSION_TUTORIAL_SHOWN = booleanPreferencesKey("is_permission_tutorial_shown")
        val KEY_DARK_THEME = stringPreferencesKey("dark_theme")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    override val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Default to true if not set, meaning it is the first launch
            preferences[PreferencesKeys.KEY_IS_FIRST_LAUNCH] ?: true
        }

    override val isPermissionTutorialShown: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_IS_PERMISSION_TUTORIAL_SHOWN] ?: false
        }

    override val darkThemePreference: Flow<Boolean?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[PreferencesKeys.KEY_DARK_THEME]) {
                "LIGHT" -> false
                "DARK" -> true
                else -> null
            }
        }

    override val languagePreference: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_LANGUAGE] ?: "en"
        }

    override val isDeveloperModeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_DEVELOPER_MODE] ?: false
        }

    override val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEY_DYNAMIC_COLOR] ?: false
        }

    override suspend fun setFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_FIRST_LAUNCH] = !completed
        }
    }

    override suspend fun setPermissionTutorialShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PERMISSION_TUTORIAL_SHOWN] = shown
        }
    }

    override suspend fun setDarkThemePreference(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            when (enabled) {
                true -> preferences[PreferencesKeys.KEY_DARK_THEME] = "DARK"
                false -> preferences[PreferencesKeys.KEY_DARK_THEME] = "LIGHT"
                null -> preferences[PreferencesKeys.KEY_DARK_THEME] = "SYSTEM"
            }
        }
    }

    override suspend fun setLanguagePreference(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_LANGUAGE] = language
        }
    }

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DEVELOPER_MODE] = enabled
        }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DYNAMIC_COLOR] = enabled
        }
    }

    override suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
