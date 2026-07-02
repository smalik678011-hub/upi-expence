package com.example.presentation.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.utils.NotificationPermissionHelper
import com.example.presentation.screens.components.PreferenceCategoryHeader
import com.example.presentation.screens.components.PreferenceItemCard
import com.example.presentation.screens.components.PreferenceStatItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    viewModel: SettingsViewModel,
    onNavigateToAppearance: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isDeveloperModeEnabled by viewModel.isDeveloperModeEnabled.collectAsStateWithLifecycle()
    val totalTransactions by viewModel.totalTransactions.collectAsStateWithLifecycle()
    val totalSentAmount by viewModel.totalSentAmount.collectAsStateWithLifecycle()
    val totalReceivedAmount by viewModel.totalReceivedAmount.collectAsStateWithLifecycle()
    val databaseSizeKb by viewModel.databaseSizeKb.collectAsStateWithLifecycle()
    val databaseVersion by viewModel.databaseVersion.collectAsStateWithLifecycle()

    // Dialog state
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }

    // Notification Access Permission State
    var isNotificationAccessGranted by remember {
        mutableStateOf(NotificationPermissionHelper.isNotificationListenerEnabled(context))
    }

    // Refresh db stats
    LaunchedEffect(Unit) {
        viewModel.updateDatabaseStats(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_main_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("settings_back_button")
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // --- SECTION 1: APPEARANCE ---
            PreferenceCategoryHeader(title = stringResource(R.string.settings_appearance_title))
            PreferenceItemCard(
                title = stringResource(R.string.settings_appearance_title),
                subtitle = stringResource(R.string.settings_appearance_desc),
                icon = Icons.Default.Palette,
                onClick = onNavigateToAppearance,
                testTag = "pref_appearance_button"
            )

            // --- SECTION 2: NOTIFICATIONS ---
            PreferenceCategoryHeader(title = stringResource(R.string.settings_notifications_title))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("pref_notification_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Listener Access",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        
                        // Status Badge
                        AssistChip(
                            onClick = {
                                isNotificationAccessGranted = NotificationPermissionHelper.isNotificationListenerEnabled(context)
                            },
                            label = {
                                Text(
                                    text = if (isNotificationAccessGranted) "Active" else "Inactive",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = if (isNotificationAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                containerColor = if (isNotificationAccessGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Text(
                        text = "The app parses push alerts from GPay, Paytm, and PhonePe locally on your device to create your financial logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                context.startActivity(NotificationPermissionHelper.getIntentForNotificationListenerSettings())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_notif_settings")
                        ) {
                            Text("Open Settings")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                isNotificationAccessGranted = NotificationPermissionHelper.isNotificationListenerEnabled(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_refresh_notif_status")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Status")
                        }
                    }
                }
            }

            // --- SECTION 3: DATA MANAGEMENT ---
            PreferenceCategoryHeader(title = stringResource(R.string.settings_data_title))
            PreferenceStatItem(
                label = stringResource(R.string.data_stat_transactions),
                value = "$totalTransactions",
                icon = Icons.Default.TrendingUp,
                testTag = "stat_total_transactions"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PreferenceStatItem(
                        label = stringResource(R.string.data_stat_sent),
                        value = "₹${"%,.2f".format(totalSentAmount)}",
                        icon = Icons.Default.ArrowUpward,
                        testTag = "stat_sent_amount",
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PreferenceStatItem(
                        label = stringResource(R.string.data_stat_received),
                        value = "₹${"%,.2f".format(totalReceivedAmount)}",
                        icon = Icons.Default.ArrowDownward,
                        testTag = "stat_received_amount",
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PreferenceStatItem(
                        label = stringResource(R.string.data_stat_db_size),
                        value = "%.2f KB".format(databaseSizeKb),
                        icon = Icons.Default.SdStorage,
                        testTag = "stat_db_size",
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PreferenceStatItem(
                        label = stringResource(R.string.data_stat_db_version),
                        value = "v$databaseVersion",
                        icon = Icons.Default.SettingsInputAntenna,
                        testTag = "stat_db_version",
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
            }

            // Report Export Card
            PreferenceItemCard(
                title = "Export Reports",
                subtitle = "Generate standards-compliant CSV and professional multi-page PDF reports offline.",
                icon = Icons.Default.Description,
                onClick = onNavigateToExport,
                testTag = "pref_export_reports_button"
            )

            // Data actions
            PreferenceItemCard(
                title = stringResource(R.string.data_btn_clear_transactions),
                subtitle = stringResource(R.string.data_btn_clear_desc),
                icon = Icons.Default.LayersClear,
                onClick = { showClearConfirmDialog = true },
                testTag = "btn_clear_transactions"
            )
            PreferenceItemCard(
                title = stringResource(R.string.data_btn_delete_all),
                subtitle = stringResource(R.string.data_btn_delete_desc),
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteAllConfirmDialog = true },
                testTag = "btn_delete_all_data"
            )

            // --- SECTION 4: PRIVACY ---
            PreferenceCategoryHeader(title = stringResource(R.string.settings_privacy_title))
            PreferenceItemCard(
                title = stringResource(R.string.settings_privacy_title),
                subtitle = stringResource(R.string.settings_privacy_desc),
                icon = Icons.Default.Shield,
                onClick = onNavigateToPrivacy,
                testTag = "pref_privacy_button"
            )

            // --- SECTION 5: ABOUT ---
            PreferenceCategoryHeader(title = stringResource(R.string.settings_about_title))
            PreferenceItemCard(
                title = stringResource(R.string.settings_about_title),
                subtitle = stringResource(R.string.settings_about_desc),
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout,
                testTag = "pref_about_button"
            )

            // --- SECTION 6: DEVELOPER OPTIONS ---
            if (isDeveloperModeEnabled) {
                PreferenceCategoryHeader(title = stringResource(R.string.settings_developer_title))
                PreferenceItemCard(
                    title = stringResource(R.string.settings_developer_title),
                    subtitle = stringResource(R.string.settings_developer_desc),
                    icon = Icons.Default.BugReport,
                    onClick = onNavigateToDeveloper,
                    testTag = "pref_developer_button"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- DIALOG 1: CLEAR TRANSACTIONS CONFIRMATION ---
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(text = stringResource(R.string.data_confirm_title)) },
            text = { Text(text = stringResource(R.string.data_confirm_clear_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllTransactions {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.data_undo_snack),
                                    actionLabel = context.getString(R.string.data_undo_btn),
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoClearTransactions {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Transactions restored!")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_clear_confirm_btn")
                ) {
                    Text(text = stringResource(R.string.data_btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.testTag("dialog_clear_cancel_btn")
                ) {
                    Text(text = stringResource(R.string.data_btn_cancel))
                }
            },
            modifier = Modifier.testTag("dialog_clear_transactions")
        )
    }

    // --- DIALOG 2: DELETE ALL DATA & RESET CONFIRMATION ---
    if (showDeleteAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmDialog = false },
            title = { Text(text = stringResource(R.string.data_confirm_title)) },
            text = { Text(text = stringResource(R.string.data_confirm_delete_all_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirmDialog = false
                        viewModel.deleteAllDataAndReset(context) {
                            // Close app or restart
                            (context as? Activity)?.finishAffinity()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_delete_confirm_btn")
                ) {
                    Text(text = stringResource(R.string.data_btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllConfirmDialog = false },
                    modifier = Modifier.testTag("dialog_delete_cancel_btn")
                ) {
                    Text(text = stringResource(R.string.data_btn_cancel))
                }
            },
            modifier = Modifier.testTag("dialog_delete_all")
        )
    }
}
