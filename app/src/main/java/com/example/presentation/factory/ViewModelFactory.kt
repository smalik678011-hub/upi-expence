package com.example.presentation.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.di.AppContainer

class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(com.example.presentation.screens.FoundationDashboardViewModel::class.java) -> {
                com.example.presentation.screens.FoundationDashboardViewModel(
                    expenseRepository = appContainer.expenseRepository,
                    settingsRepository = appContainer.settingsRepository,
                    notificationRepository = appContainer.notificationRepository,
                    dispatcherProvider = appContainer.dispatcherProvider,
                    logger = appContainer.logger,
                    secureStorage = appContainer.secureStorage
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.HomeViewModel::class.java) -> {
                com.example.presentation.screens.HomeViewModel(
                    expenseRepository = appContainer.expenseRepository,
                    dispatcherProvider = appContainer.dispatcherProvider,
                    logger = appContainer.logger
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.OnboardingViewModel::class.java) -> {
                com.example.presentation.screens.OnboardingViewModel(
                    appPreferencesRepository = appContainer.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.PermissionViewModel::class.java) -> {
                com.example.presentation.screens.PermissionViewModel(
                    appPreferencesRepository = appContainer.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.SettingsViewModel::class.java) -> {
                com.example.presentation.screens.SettingsViewModel(
                    expenseRepository = appContainer.expenseRepository,
                    appPreferencesRepository = appContainer.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.AppearanceViewModel::class.java) -> {
                com.example.presentation.screens.AppearanceViewModel(
                    appPreferencesRepository = appContainer.appPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.PrivacyViewModel::class.java) -> {
                com.example.presentation.screens.PrivacyViewModel() as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.AboutViewModel::class.java) -> {
                com.example.presentation.screens.AboutViewModel() as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.TransactionExplorerViewModel::class.java) -> {
                com.example.presentation.screens.TransactionExplorerViewModel(
                    expenseRepository = appContainer.expenseRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.TransactionDetailsViewModel::class.java) -> {
                com.example.presentation.screens.TransactionDetailsViewModel(
                    expenseRepository = appContainer.expenseRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.ParserValidationViewModel::class.java) -> {
                com.example.presentation.screens.ParserValidationViewModel(
                    parserEngine = appContainer.parserEngine,
                    duplicateDetector = appContainer.duplicateDetector
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.ExportViewModel::class.java) -> {
                com.example.presentation.screens.ExportViewModel(
                    exportRepository = appContainer.exportRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
