package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.core.log.AndroidLogger
import com.example.core.security.SharedPreferencesSecureStorage
import com.example.core.utils.DefaultDispatcherProvider
import com.example.data.repository.ExpenseRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.presentation.screens.FoundationDashboardScreen
import com.example.presentation.screens.FoundationDashboardViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val expenseRepository = ExpenseRepositoryImpl()
    val secureStorage = SharedPreferencesSecureStorage(context)
    val settingsRepository = SettingsRepositoryImpl(secureStorage)
    val dispatcherProvider = DefaultDispatcherProvider()
    val logger = AndroidLogger()
    val notificationRepository = com.example.data.repository.NotificationRepositoryImpl()
    val viewModel = FoundationDashboardViewModel(
        expenseRepository = expenseRepository,
        settingsRepository = settingsRepository,
        notificationRepository = notificationRepository,
        dispatcherProvider = dispatcherProvider,
        logger = logger,
        secureStorage = secureStorage
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        FoundationDashboardScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
