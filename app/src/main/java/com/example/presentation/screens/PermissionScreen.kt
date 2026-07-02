package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.core.utils.BatteryOptimizationHelper
import com.example.core.utils.NotificationPermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // Refresh permissions on resume automatically
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Run first check on mount
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
        viewModel.setTutorialShown()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("permission_screen_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.perm_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("permission_lazycolumn"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Permission Status Card
                item {
                    val statusText = if (uiState.isNotificationListenerGranted) {
                        stringResource(R.string.perm_status_granted)
                    } else {
                        stringResource(R.string.perm_status_denied)
                    }
                    val statusColor = if (uiState.isNotificationListenerGranted) {
                        Color(0xFFECFDF5)
                    } else {
                        Color(0xFFFFF1F2)
                    }
                    val contentColor = if (uiState.isNotificationListenerGranted) {
                        Color(0xFF047857)
                    } else {
                        Color(0xFFBE123C)
                    }
                    val icon = if (uiState.isNotificationListenerGranted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Warning
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("permission_status_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Status",
                                tint = contentColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = String.format(stringResource(R.string.perm_status), statusText),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isNotificationListenerGranted) {
                                        "Automated tracking is active and listening for transaction SMS alerts."
                                    } else {
                                        "Action required: Offline tracking cannot capture receipts yet."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // 2. Main Explanation Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "How Offline Automation Works",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.perm_explanation),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Button(
                                onClick = {
                                    context.startActivity(
                                        NotificationPermissionHelper.getIntentForNotificationListenerSettings()
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grant_permission_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.perm_btn_grant),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // 3. Battery Optimization (Optional) Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatterySaver,
                                        contentDescription = "Battery info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.battery_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                val batteryIgnoredText = if (uiState.isBatteryOptimizationIgnored) "Unrestricted" else "Optimizing"
                                val batteryIgnoredColor = if (uiState.isBatteryOptimizationIgnored) Color(0xFF047857) else MaterialTheme.colorScheme.primary
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(batteryIgnoredColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = batteryIgnoredText.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = batteryIgnoredColor
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.battery_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )

                            if (!uiState.isBatteryOptimizationIgnored) {
                                Button(
                                    onClick = {
                                        context.startActivity(
                                            BatteryOptimizationHelper.getIntentForBatteryOptimizationSettings()
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("battery_exempt_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.battery_btn_exempt),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            )

                            // OEM Dropdown selection
                            Text(
                                text = stringResource(R.string.battery_oem_guide_title),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(R.string.battery_oem_select_brand),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            var expandedBrandSelector by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedBrandSelector = true }
                                        .testTag("oem_selector_card"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (uiState.selectedOemBrand.isEmpty()) {
                                                "Tap to choose manufacturer..."
                                            } else {
                                                uiState.selectedOemBrand
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (uiState.selectedOemBrand.isEmpty()) {
                                                MaterialTheme.colorScheme.outline
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand brand dropdown"
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedBrandSelector,
                                    onDismissRequest = { expandedBrandSelector = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    BatteryOptimizationHelper.supportedOems.forEach { brand ->
                                        DropdownMenuItem(
                                            text = { Text(text = brand) },
                                            onClick = {
                                                viewModel.selectOemBrand(brand)
                                                expandedBrandSelector = false
                                            }
                                        )
                                    }
                                }
                            }

                            // If a brand is selected, show its instruction guide
                            if (uiState.oemBrandGuidance.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "${uiState.selectedOemBrand} Instructions:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.oemBrandGuidance,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.checkPermissions(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("refresh_status_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh status icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.perm_btn_refresh),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = onNavigateToHome,
                        enabled = uiState.isNotificationListenerGranted,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("continue_to_app_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.perm_btn_continue),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
