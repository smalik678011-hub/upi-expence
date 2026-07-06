package com.example.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.utils.NotificationPermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var animationFinished by remember { mutableStateOf(false) }

    // Spring animation states for premium bouncy entrance
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run scale and fade animation concurrently
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        // Ensure branding stays visible for at least 1.6 seconds for premium polish
        delay(1600)
        animationFinished = true
    }

    LaunchedEffect(isFirstLaunch, animationFinished) {
        val firstLaunch = isFirstLaunch ?: return@LaunchedEffect
        if (!animationFinished) return@LaunchedEffect
        
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B), // Deep Indigo
                        Color(0xFF0F172A)  // Slate Dark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Elegant Vector Logo Card
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2563EB), // Vibrant Blue
                                Color(0xFF1E1B4B)  // Deep Indigo
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Foreground Layer (Shield + Rupee Motif)
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "UPI Expense Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Application Title
            Text(
                text = "UPI Unified",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Expense Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF38BDF8), // Electric blue accent
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "Track, analyze, and secure digital payments",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8), // Muted slate gray
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Subtle Material Progress
            CircularProgressIndicator(
                color = Color(0xFF38BDF8),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

