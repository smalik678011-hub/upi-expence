package com.example.presentation.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.core.utils.NotificationPermissionHelper

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentStep by remember { mutableStateOf(0) }

    // Auto-detect permission grant when returning to app on step 2 (0-indexed third step)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (currentStep == 2 && NotificationPermissionHelper.isNotificationListenerEnabled(context)) {
                    viewModel.completeOnboarding()
                    onNavigateNext()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding / Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < 2) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onNavigateNext()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_btn_skip),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Animated Screen content based on currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "OnboardingStepAnimation"
                ) { step ->
                    when (step) {
                        0 -> OnboardingStepOne()
                        1 -> OnboardingStepTwo()
                        2 -> OnboardingStepThree(
                            onEnableClick = {
                                context.startActivity(
                                    NotificationPermissionHelper.getIntentForNotificationListenerSettings()
                                )
                            }
                        )
                    }
                }
            }

            // Bottom Navigation (Dots and buttons)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Stepper Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = index == currentStep
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Primary Action Button
                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep++
                        } else {
                            // If they click on step 3 without enabling yet, or if they have enabled it
                            if (NotificationPermissionHelper.isNotificationListenerEnabled(context)) {
                                viewModel.completeOnboarding()
                                onNavigateNext()
                            } else {
                                // Direct to settings
                                context.startActivity(
                                    NotificationPermissionHelper.getIntentForNotificationListenerSettings()
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_primary_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (currentStep) {
                                0 -> stringResource(R.string.onboarding_btn_next)
                                1 -> stringResource(R.string.onboarding_btn_next)
                                else -> if (NotificationPermissionHelper.isNotificationListenerEnabled(context)) {
                                    stringResource(R.string.onboarding_btn_get_started)
                                } else {
                                    stringResource(R.string.perm_btn_grant)
                                }
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepOne() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Shield Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_s1_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.onboarding_s1_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Features list
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingFeatureRow("100% Offline App (No servers)")
            OnboardingFeatureRow("No Bank Login Details Required")
            OnboardingFeatureRow("No Invasive SMS Permissions Needed")
        }
    }
}

@Composable
fun OnboardingStepTwo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Notification Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_s2_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.onboarding_s2_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Features list
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingFeatureRow("Reads only UPI push notifications")
            OnboardingFeatureRow("Ignores personal chat alerts")
            OnboardingFeatureRow("No data ever leaves the device")
        }
    }
}

@Composable
fun OnboardingStepThree(
    onEnableClick: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = NotificationPermissionHelper.isNotificationListenerEnabled(context)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (hasPermission) Color(0xFFECFDF5) else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Security,
                contentDescription = "Security Icon",
                tint = if (hasPermission) Color(0xFF047857) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasPermission) "All Set!" else stringResource(R.string.onboarding_s3_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (hasPermission) {
                "Notification permission is granted. You can now proceed to explore the transaction ledger."
            } else {
                stringResource(R.string.onboarding_s3_desc)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (!hasPermission) {
            TextButton(
                onClick = onEnableClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Configure manually in Settings →",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OnboardingFeatureRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Check",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
