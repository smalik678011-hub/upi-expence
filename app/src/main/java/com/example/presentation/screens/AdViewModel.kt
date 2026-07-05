package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AdRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdViewModel(
    private val adRepository: AdRepository
) : ViewModel() {

    val isPremiumUser: StateFlow<Boolean> = adRepository.isPremiumUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isAdPersonalizationEnabled: StateFlow<Boolean> = adRepository.isAdPersonalizationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setPremiumUser(isPremium: Boolean) {
        viewModelScope.launch {
            adRepository.setPremiumUser(isPremium)
        }
    }

    fun setAdPersonalizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            adRepository.setAdPersonalizationEnabled(enabled)
        }
    }
}
