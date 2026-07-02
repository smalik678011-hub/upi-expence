package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.core.utils.NotificationPermissionHelper

@Composable
fun StartupRouterScreen(
    onboardingViewModel: OnboardingViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToPermission: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFirstLaunch by onboardingViewModel.isFirstLaunch.collectAsState()

    LaunchedEffect(isFirstLaunch) {
        val firstLaunch = isFirstLaunch ?: return@LaunchedEffect // Wait until loaded
        
        if (firstLaunch) {
            onNavigateToOnboarding()
        } else {
            val isPermissionGranted = NotificationPermissionHelper.isNotificationListenerEnabled(context)
            if (isPermissionGranted) {
                onNavigateToHome()
            } else {
                onNavigateToPermission()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
