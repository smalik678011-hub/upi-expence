package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.UpiExpenseApplication
import com.example.core.utils.NotificationPermissionHelper
import com.example.presentation.factory.ViewModelFactory
import com.example.presentation.screens.*

sealed class Screen(val route: String) {
    object StartupRouter : Screen("startup_router")
    object Onboarding : Screen("onboarding")
    object Permission : Screen("permission")
    object Privacy : Screen("privacy")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object SettingsAppearance : Screen("settings_appearance")
    object SettingsAbout : Screen("settings_about")
    object SettingsDeveloper : Screen("settings_developer")
    object ParserValidation : Screen("parser_validation")
    object TransactionExplorer : Screen("transaction_explorer")
    object TransactionDetails : Screen("transaction_details/{id}") {
        fun createRoute(id: String) = "transaction_details/$id"
    }
    object ExportReport : Screen("export_report")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current.applicationContext as UpiExpenseApplication
    val appContainer = context.container
    val factory = ViewModelFactory(appContainer)

    NavHost(
        navController = navController,
        startDestination = Screen.StartupRouter.route,
        modifier = modifier
    ) {
        composable(Screen.StartupRouter.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
            StartupRouterScreen(
                onboardingViewModel = onboardingViewModel,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.StartupRouter.route) { inclusive = true }
                    }
                },
                onNavigateToPermission = {
                    navController.navigate(Screen.Permission.route) {
                        popUpTo(Screen.StartupRouter.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.StartupRouter.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onNavigateNext = {
                    val isGranted = NotificationPermissionHelper.isNotificationListenerEnabled(context)
                    val targetRoute = if (isGranted) Screen.Home.route else Screen.Permission.route
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permission.route) {
            val permissionViewModel: PermissionViewModel = viewModel(factory = factory)
            PermissionScreen(
                viewModel = permissionViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.Privacy.route)
                },
                onNavigateToExplorer = {
                    navController.navigate(Screen.TransactionExplorer.route)
                },
                onNavigateToDetails = { id ->
                    navController.navigate(Screen.TransactionDetails.createRoute(id))
                }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            SettingsMainScreen(
                viewModel = settingsViewModel,
                onNavigateToAppearance = {
                    navController.navigate(Screen.SettingsAppearance.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.Privacy.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.SettingsAbout.route)
                },
                onNavigateToDeveloper = {
                    navController.navigate(Screen.SettingsDeveloper.route)
                },
                onNavigateToExport = {
                    navController.navigate(Screen.ExportReport.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ExportReport.route) {
            val exportViewModel: ExportViewModel = viewModel(factory = factory)
            ExportScreen(
                viewModel = exportViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SettingsAppearance.route) {
            val appearanceViewModel: AppearanceViewModel = viewModel(factory = factory)
            SettingsAppearanceScreen(
                viewModel = appearanceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SettingsAbout.route) {
            val aboutViewModel: AboutViewModel = viewModel(factory = factory)
            SettingsAboutScreen(
                viewModel = aboutViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SettingsDeveloper.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            SettingsDeveloperScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToParserValidation = {
                    navController.navigate(Screen.ParserValidation.route)
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val viewModel: FoundationDashboardViewModel = viewModel(factory = factory)
            FoundationDashboardScreen(viewModel = viewModel)
        }

        composable(Screen.TransactionExplorer.route) {
            val explorerViewModel: TransactionExplorerViewModel = viewModel(factory = factory)
            TransactionExplorerScreen(
                viewModel = explorerViewModel,
                onNavigateToDetails = { id ->
                    navController.navigate(Screen.TransactionDetails.createRoute(id))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TransactionDetails.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val detailsViewModel: TransactionDetailsViewModel = viewModel(factory = factory)
            TransactionDetailsScreen(
                id = id,
                viewModel = detailsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ParserValidation.route) {
            val parserValidationViewModel: ParserValidationViewModel = viewModel(factory = factory)
            ParserValidationScreen(
                viewModel = parserValidationViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
