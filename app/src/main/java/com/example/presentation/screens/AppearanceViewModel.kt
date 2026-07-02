package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val darkThemePreference: StateFlow<Boolean?> = appPreferencesRepository.darkThemePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isDynamicColorEnabled: StateFlow<Boolean> = appPreferencesRepository.isDynamicColorEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val languagePreference: StateFlow<String> = appPreferencesRepository.languagePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "en"
        )

    fun setDarkThemePreference(enabled: Boolean?) {
        viewModelScope.launch {
            appPreferencesRepository.setDarkThemePreference(enabled)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setDynamicColorEnabled(enabled)
        }
    }

    fun setLanguagePreference(language: String) {
        viewModelScope.launch {
            appPreferencesRepository.setLanguagePreference(language)
        }
    }
}
