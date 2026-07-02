package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.presentation.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = (applicationContext as UpiExpenseApplication).container
            val appPreferencesRepository = appContainer.appPreferencesRepository
            
            val darkThemePref by appPreferencesRepository.darkThemePreference.collectAsState(initial = null)
            val dynamicColorPref by appPreferencesRepository.isDynamicColorEnabled.collectAsState(initial = false)
            
            val isDarkTheme = when (darkThemePref) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColorPref
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
