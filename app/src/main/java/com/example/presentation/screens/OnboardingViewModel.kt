package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val isFirstLaunch: StateFlow<Boolean?> = appPreferencesRepository.isFirstLaunch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferencesRepository.setFirstLaunchCompleted(true)
        }
    }
}
