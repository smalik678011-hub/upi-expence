package com.example.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.presentation.screens.components.PreferenceCategoryHeader
import com.example.presentation.screens.components.PreferenceItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(
    viewModel: AboutViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    val appVersion by viewModel.appVersion.collectAsStateWithLifecycle()
    val versionCode by viewModel.versionCode.collectAsStateWithLifecycle()
    val buildType by viewModel.buildType.collectAsStateWithLifecycle()

    var showLicensesDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAppInfo(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_about_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("about_back_button")
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
            
            // --- HEADER LOGO SLOT ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "App Emblem",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Text(
                    text = "UPI Expense Tracker",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Local & Encrypted Personal Ledger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // --- SECTION 1: BUILD INFORMATION ---
            PreferenceCategoryHeader(title = "Build Details")
            
            // Developer Name
            PreferenceItemCard(
                title = stringResource(R.string.about_dev_name),
                subtitle = stringResource(R.string.about_dev_value),
                icon = Icons.Default.Engineering,
                onClick = {},
                testTag = "pref_dev_name"
            )

            // App Version
            PreferenceItemCard(
                title = stringResource(R.string.about_version_title),
                subtitle = appVersion,
                icon = Icons.Default.Info,
                onClick = {},
                testTag = "pref_app_version"
            )

            // Version Code
            PreferenceItemCard(
                title = stringResource(R.string.about_version_code_title),
                subtitle = "$versionCode",
                icon = Icons.Default.Numbers,
                onClick = {},
                testTag = "pref_version_code"
            )

            // Build Type
            PreferenceItemCard(
                title = stringResource(R.string.about_build_type),
                subtitle = buildType.uppercase(),
                icon = Icons.Default.Code,
                onClick = {},
                testTag = "pref_build_type"
            )

            // --- SECTION 2: DISCOVER ---
            PreferenceCategoryHeader(title = "General")

            // What's New
            PreferenceItemCard(
                title = stringResource(R.string.about_whats_new_title),
                subtitle = stringResource(R.string.about_whats_new_desc),
                icon = Icons.Default.NewReleases,
                onClick = { showWhatsNewDialog = true },
                testTag = "pref_whats_new"
            )

            // Open Source Licenses
            PreferenceItemCard(
                title = stringResource(R.string.about_licenses_title),
                subtitle = "View software libraries and notices used in production",
                icon = Icons.Default.Terminal,
                onClick = { showLicensesDialog = true },
                testTag = "pref_licenses"
            )

            // Rate App
            PreferenceItemCard(
                title = stringResource(R.string.about_rate_title),
                subtitle = stringResource(R.string.about_rate_desc),
                icon = Icons.Default.Star,
                onClick = {
                    // Future intent placeholder or open Play Store
                },
                testTag = "pref_rate_app"
            )

            // Share App
            PreferenceItemCard(
                title = stringResource(R.string.about_share_title),
                subtitle = stringResource(R.string.about_share_desc),
                icon = Icons.Default.Share,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Track your UPI expenses automatically and 100% offline using the UPI Unified Expense Tracker!")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                testTag = "pref_share_app"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- LICENSES DIALOG ---
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text(text = stringResource(R.string.about_license_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "The following libraries are included under free/open software licenses:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    listOf(
                        "Jetpack Compose (Apache 2.0)",
                        "Room Database (Apache 2.0)",
                        "DataStore Preferences (Apache 2.0)",
                        "Kotlin Coroutines (Apache 2.0)",
                        "Material Design Components (Apache 2.0)",
                        "Robolectric (MIT License)"
                    ).forEach { library ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = library,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showLicensesDialog = false },
                    modifier = Modifier.testTag("dialog_licenses_close")
                ) {
                    Text(text = stringResource(R.string.about_license_dialog_close))
                }
            },
            modifier = Modifier.testTag("dialog_licenses")
        )
    }

    // --- WHAT'S NEW DIALOG ---
    if (showWhatsNewDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsNewDialog = false },
            title = { Text(text = stringResource(R.string.about_whats_new_dialog_title)) },
            text = {
                Text(
                    text = stringResource(R.string.about_whats_new_dialog_text),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showWhatsNewDialog = false },
                    modifier = Modifier.testTag("dialog_whats_new_close")
                ) {
                    Text(text = "Close")
                }
            },
            modifier = Modifier.testTag("dialog_whats_new")
        )
    }
}
