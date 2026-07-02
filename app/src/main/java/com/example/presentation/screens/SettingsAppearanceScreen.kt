package com.example.presentation.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.presentation.screens.components.PreferenceCategoryHeader
import com.example.presentation.screens.components.PreferenceItemCard
import com.example.presentation.screens.components.PreferenceSwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(
    viewModel: AppearanceViewModel,
    onNavigateBack: () -> Unit
) {
    val darkThemePref by viewModel.darkThemePreference.collectAsStateWithLifecycle()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    val languagePref by viewModel.languagePreference.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val themeLabel = when (darkThemePref) {
        true -> stringResource(R.string.appearance_theme_dark)
        false -> stringResource(R.string.appearance_theme_light)
        null -> stringResource(R.string.appearance_theme_system)
    }

    val languageLabel = when (languagePref) {
        "hi" -> stringResource(R.string.appearance_lang_hi)
        else -> stringResource(R.string.appearance_lang_en)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_appearance_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("appearance_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: THEME & COLOR ---
            PreferenceCategoryHeader(title = "Styling")
            
            // Theme Mode Selector
            PreferenceItemCard(
                title = stringResource(R.string.appearance_theme_mode),
                subtitle = themeLabel,
                icon = Icons.Default.BrightnessMedium,
                onClick = { showThemeDialog = true },
                testTag = "pref_theme_mode_selector"
            )

            // Dynamic Colors (Material You) Toggle (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PreferenceSwitchItem(
                    title = stringResource(R.string.appearance_material_you_title),
                    subtitle = stringResource(R.string.appearance_material_you_desc),
                    icon = Icons.Default.ColorLens,
                    checked = isDynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) },
                    testTag = "pref_material_you_toggle"
                )
            } else {
                PreferenceItemCard(
                    title = stringResource(R.string.appearance_material_you_title),
                    subtitle = "Not supported on this Android version (Requires API 31+)",
                    icon = Icons.Default.ColorLens,
                    onClick = {},
                    testTag = "pref_material_you_unsupported",
                    trailingContent = {
                        Switch(checked = false, onCheckedChange = null, enabled = false)
                    }
                )
            }

            // --- SECTION 2: REGIONAL ---
            PreferenceCategoryHeader(title = "Regional & Language")
            
            // App Language Selector
            PreferenceItemCard(
                title = stringResource(R.string.appearance_language_title),
                subtitle = languageLabel,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true },
                testTag = "pref_language_selector"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- THEME SELECTOR DIALOG ---
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = stringResource(R.string.appearance_theme_mode)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        null to stringResource(R.string.appearance_theme_system),
                        false to stringResource(R.string.appearance_theme_light),
                        true to stringResource(R.string.appearance_theme_dark)
                    )

                    themes.forEach { (themeValue, themeName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDarkThemePreference(themeValue)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = darkThemePref == themeValue,
                                onClick = {
                                    viewModel.setDarkThemePreference(themeValue)
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = themeName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showThemeDialog = false },
                    modifier = Modifier.testTag("dialog_theme_close")
                ) {
                    Text(text = "Close")
                }
            },
            modifier = Modifier.testTag("dialog_theme_mode")
        )
    }

    // --- LANGUAGE SELECTOR DIALOG ---
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.appearance_language_title)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        "en" to stringResource(R.string.appearance_lang_en),
                        "hi" to stringResource(R.string.appearance_lang_hi)
                    )

                    languages.forEach { (langCode, langName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguagePreference(langCode)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RadioButton(
                                selected = languagePref == langCode,
                                onClick = {
                                    viewModel.setLanguagePreference(langCode)
                                    showLanguageDialog = false
                                }
                            )
                            Text(
                                text = langName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showLanguageDialog = false },
                    modifier = Modifier.testTag("dialog_lang_close")
                ) {
                    Text(text = "Close")
                }
            },
            modifier = Modifier.testTag("dialog_language")
        )
    }
}
