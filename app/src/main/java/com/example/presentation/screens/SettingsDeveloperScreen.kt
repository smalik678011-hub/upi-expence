package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.log.InMemoryLogStore
import com.example.presentation.screens.components.PreferenceCategoryHeader
import com.example.presentation.screens.components.PreferenceItemCard
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDeveloperScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToParserValidation: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Parser Logs", "Notif Logs", "Raw Notifs")

    val parserLogs by InMemoryLogStore.parserLogs.collectAsStateWithLifecycle()
    val notificationLogs by InMemoryLogStore.notificationLogs.collectAsStateWithLifecycle()
    val rawNotifications by InMemoryLogStore.rawNotifications.collectAsStateWithLifecycle()

    val totalTransactions by viewModel.totalTransactions.collectAsStateWithLifecycle()
    val databaseSizeKb by viewModel.databaseSizeKb.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_developer_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dev_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("developer_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Export Logs Button
                    IconButton(
                        onClick = {
                            val logText = StringBuilder().apply {
                                appendLine("=== UPI EXPENSE TRACKER DIAGNOSTIC LOGS ===")
                                appendLine("Database Size: %.2f KB".format(databaseSizeKb))
                                appendLine("Total Transactions: $totalTransactions")
                                appendLine("\n--- PARSER LOGS ---")
                                parserLogs.forEach { appendLine(it) }
                                appendLine("\n--- NOTIFICATION LOGS ---")
                                notificationLogs.forEach { appendLine(it) }
                            }.toString()

                            val clip = ClipData.newPlainText("diagnostic_logs", logText)
                            clipboardManager.setPrimaryClip(clip)
                            
                            androidx.compose.runtime.snapshots.Snapshot.withoutReadObservation {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Logs exported to clipboard!")
                                }
                            }
                        },
                        modifier = Modifier.testTag("btn_export_logs_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Export Logs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Clear Logs Button
                    IconButton(
                        onClick = {
                            InMemoryLogStore.clearAllLogs()
                            androidx.compose.runtime.snapshots.Snapshot.withoutReadObservation {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Diagnostic logs cleared!")
                                }
                            }
                        },
                        modifier = Modifier.testTag("btn_clear_logs_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = MaterialTheme.colorScheme.error
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
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // --- DATABASE QUICK PANEL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("dev_db_panel"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Local SQLite & Schema Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Table: 'expenses'", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Records count: $totalTransactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Size: %.2f KB".format(databaseSizeKb), style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Version: v1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Mock actions / Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Add a dummy log statement for inspection
                                InMemoryLogStore.addParserLog("USER TRIGGERED: Dummy parsing simulation started.")
                                InMemoryLogStore.addParserLog("SUCCESS: Parsed simulated GPay transaction (Ref: 129481923, Amt: ₹250.00)")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_mock_parse_log")
                        ) {
                            Text("Simulate Parse Log", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                InMemoryLogStore.addNotificationLog("USER TRIGGERED: Triggered diagnostic listener pulse.")
                                InMemoryLogStore.addNotificationLog("CAPTURED: Simulating incoming WhatsApp/SMS verification alert.")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_mock_notif_log")
                        ) {
                            Text("Simulate Notif Log", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onNavigateToParserValidation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_launch_parser_validator"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Parser Validator", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- TAB ROW FOR LOG VIEWERS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dev_logs_tabrow")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) },
                        modifier = Modifier.testTag("dev_tab_$index")
                    )
                }
            }

            // --- LOG STREAMS CONTENT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                when (selectedTabIndex) {
                    0 -> LogStreamListView(logs = parserLogs, testTag = "parser_logs_list")
                    1 -> LogStreamListView(logs = notificationLogs, testTag = "notification_logs_list")
                    2 -> RawNotificationsListView(notifications = rawNotifications, testTag = "raw_notifications_list")
                }
            }
        }
    }
}

@Composable
fun LogStreamListView(
    logs: List<String>,
    testTag: String
) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.dev_no_logs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(testTag),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        ),
                        color = if (log.contains("ERROR")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RawNotificationsListView(
    notifications: List<com.example.domain.model.NotificationData>,
    testTag: String
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No raw notifications captured from system listeners yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(testTag),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notif.packageName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(notif.postTime)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        if (notif.title != null) {
                            Text(
                                text = "Title: ${notif.title}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        
                        if (notif.text != null) {
                            Text(
                                text = "Body: ${notif.text}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (notif.extras.isNotEmpty()) {
                            Text(
                                text = "Extras: ${notif.extras}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
