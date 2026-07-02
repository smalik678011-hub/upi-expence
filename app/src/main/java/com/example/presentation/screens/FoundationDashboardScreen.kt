package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.utils.IndianFormattingUtils
import com.example.presentation.components.*
import java.util.Date

@Composable
fun FoundationDashboardScreen(
    viewModel: FoundationDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()
    val smsTrackingEnabled by viewModel.smsTrackingEnabled.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val testRupeeAmount by viewModel.testRupeeAmount.collectAsStateWithLifecycle()
    val notificationListenerEnabled by viewModel.notificationListenerEnabled.collectAsStateWithLifecycle()
    val capturedNotifications by viewModel.capturedNotifications.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showLoadingDialog by remember { mutableStateOf(false) }
    var showErrorCard by remember { mutableStateOf(false) }
    var showEmptyStateDemo by remember { mutableStateOf(false) }

    if (showLoadingDialog) {
        ReusableLoadingUI(
            message = "Simulating background data sync...",
            onDismissRequest = { showLoadingDialog = false }
        )
        // Auto dismiss after 2 seconds for smooth UX
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showLoadingDialog = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom Professional Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "UPI Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Profile JD
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Notification Permission Banner
            if (!notificationListenerEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notification_permission_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Permission Required",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Capture Disabled",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Grant notification listener access to automatically capture transactional notifications from GPay, PhonePe, Paytm, and Navi offline.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(com.example.core.utils.NotificationPermissionHelper.getIntentForNotificationListenerSettings())
                                    } catch (e: Exception) {
                                        // Safe fallback
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text(
                                    text = "Enable Listener Access",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Android 13+ / Dev Sideload Troubleshooting:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Because this is a developer build, Android may gray out the option as a \"Restricted setting\" or show an error. To fix this:\n\n1. Open device Settings -> Apps -> All Apps.\n2. Tap \"UPI Unified Expense Tracker\".\n3. Tap the 3 dots menu in the top-right corner.\n4. Select \"Allow restricted settings\" and enter PIN.\n5. Return here and click \"Enable Listener Access\" again.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Hero Title Card - Styled exactly like Design HTML's Primary Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_header_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Live Sync Active Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color.Green)
                            )
                            Text(
                                text = "SYNC ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "LAKH / CRORE SANDBOX BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = IndianFormattingUtils.formatIndianCurrency(testRupeeAmount),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.status_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Text(
                            text = statusMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verified Stable Foundation",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        
                        Button(
                            onClick = { showLoadingDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.heightIn(min = 36.dp)
                        ) {
                            Text(
                                text = "Sync Now",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Section 1: Architecture Status List
            Text(
                text = "SYSTEM ARCHITECTURE CHECKLIST",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )

            AppCard(testTag = "architecture_checklist_card") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ChecklistRow(
                        text = stringResource(R.string.status_hilt_ok),
                        isVerified = true
                    )
                    ChecklistRow(
                        text = stringResource(R.string.status_repo_ok),
                        isVerified = true
                    )
                    ChecklistRow(
                        text = stringResource(R.string.status_secure_ok),
                        isVerified = true
                    )
                    ChecklistRow(
                        text = stringResource(R.string.status_dispatcher_ok),
                        isVerified = true
                    )
                    ChecklistRow(
                        text = "UPI Notification Capture Engine active",
                        isVerified = notificationListenerEnabled
                    )
                    ChecklistRow(
                        text = stringResource(R.string.status_theme_label),
                        isVerified = true
                    )
                }
            }

            // Section 2: Indian Formatting Sandbox
            Text(
                text = "INDIAN LOCALIZATION DEMO (₹)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )

            AppCard(testTag = "formatting_sandbox_card") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.status_india_fmt),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Currency Formatter Demonstration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Lakh / Crore Grouping:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = IndianFormattingUtils.formatIndianCurrency(testRupeeAmount),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Slider to test formatting interactively
                    Slider(
                        value = testRupeeAmount.toFloat(),
                        onValueChange = { viewModel.setTestRupeeAmount(it.toDouble()) },
                        valueRange = 0f..2000000f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "₹ 0.00",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "₹ 20,00,000.00",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Date & Number Formatters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Indian Date Formatting:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = IndianFormattingUtils.formatIndianDate(Date()),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Standard Number Grouping:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = IndianFormattingUtils.formatIndianNumber(150000L),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            // Section 3: Interactive Component Sandbox
            Text(
                text = "INTERACTIVE DESIGN SYSTEM SANDBOX",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )

            AppCard(testTag = "interactive_sandbox_card") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Verify responsive UI components and feedback loops.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryActionButton(
                            text = "Show Loading Dialog",
                            onClick = { showLoadingDialog = true },
                            modifier = Modifier.weight(1.0f),
                            icon = Icons.Default.HourglassEmpty,
                            testTag = "demo_loading_btn"
                        )
                        
                        SecondaryActionButton(
                            text = "Show Error Card",
                            onClick = { showErrorCard = !showErrorCard },
                            modifier = Modifier.weight(1.0f),
                            icon = Icons.Default.BugReport,
                            testTag = "demo_error_btn"
                        )
                    }

                    PrimaryActionButton(
                        text = if (showEmptyStateDemo) "Hide Empty State Demo" else "Show Empty State Demo",
                        onClick = { showEmptyStateDemo = !showEmptyStateDemo },
                        icon = Icons.Default.Visibility,
                        testTag = "demo_empty_btn"
                    )

                    // Error Card Injection Demo
                    if (showErrorCard) {
                        ReusableErrorUI(
                            title = "Cryptographic Failure",
                            message = "Secure preference decryption failed on a low-end emulation layer.",
                            onRetry = { showErrorCard = false }
                        )
                    }

                    // Empty State Demo
                    if (showEmptyStateDemo) {
                        EmptyStateUI(
                            title = "No expenses recorded",
                            description = "Your UPI SMS and Push notifications list is empty. Set up SMS tracking inside Settings to parse transactions.",
                            actionText = "Toggle SMS tracking",
                            onActionClick = { viewModel.toggleSmsTracking() }
                        )
                    }
                }
            }

            // Section 4: Secure Settings persistence Demo
            Text(
                text = "SECURE SETTINGS PERSISTENCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )

            AppCard(testTag = "secure_settings_card") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automated SMS Tracking",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Parse transactional SMS from verified Indian banks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = smsTrackingEnabled,
                            onCheckedChange = { viewModel.toggleSmsTracking() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("sms_tracking_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Alert Threshold limit",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Notify immediately if expense crosses: ₹ $dailyLimit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1000.0, 5000.0, 10000.0, 25000.0).forEach { limitOption ->
                            Button(
                                onClick = { viewModel.updateDailyLimit(limitOption) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dailyLimit == limitOption) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (dailyLimit == limitOption) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "₹ ${limitOption.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Real-time Captured UPI Notifications Log
            Text(
                text = "REAL-TIME CAPTURED UPI NOTIFICATIONS LOG",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            )

            AppCard(testTag = "captured_notifications_card") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This log captures incoming UPI push notifications from whitelisted apps (GPay, PhonePe, Paytm, Navi) in real-time, completely offline.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    if (capturedNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "No notifications",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Waiting for notifications from whitelisted UPI apps...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            capturedNotifications.forEach { logText ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Captured",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = logText,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistRow(
    text: String,
    isVerified: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isVerified) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (isVerified) "Verified" else "Failed",
                tint = if (isVerified) Color(0xFF047857) else Color(0xFFBE123C),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
