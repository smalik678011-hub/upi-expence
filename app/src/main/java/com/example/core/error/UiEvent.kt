package com.example.core.error

interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class Navigate(val route: String) : UiEvent
}
