package app.gonull.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.gonull.data.local.AppDatabase
import app.gonull.ui.screens.appselection.AppSelectionScreen
import app.gonull.ui.screens.appselection.AppSelectionViewModel
import app.gonull.ui.screens.home.HomeScreen
import app.gonull.ui.screens.home.HomeViewModel
import app.gonull.ui.screens.onboarding.OnboardingScreen
import app.gonull.ui.screens.intel.IntelScreen
import app.gonull.ui.screens.settings.SettingsScreen
import app.gonull.ui.screens.journal.JournalScreen
import app.gonull.ui.screens.stats.StatsScreen
import app.gonull.ui.screens.stats.StatsViewModel
import app.gonull.ui.screens.usageinsights.UsageInsightsScreen
import app.gonull.ui.screens.usageinsights.UsageInsightsViewModel
import app.gonull.util.PermissionHelper

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object UsageInsights : Screen("usage_insights")
    object Home : Screen("home")
    object AppSelection : Screen("app_selection")
    object Settings : Screen("settings")
    object Intel : Screen("intel")
    object Stats : Screen("stats")
    object Journal : Screen("journal")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.UsageInsights.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UsageInsights.route) {
            val viewModel = remember { UsageInsightsViewModel(database) }
            UsageInsightsScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            val viewModel = remember { HomeViewModel(database) }
            HomeScreen(
                viewModel = viewModel,
                database = database,
                onNavigateToAppSelection = {
                    navController.navigate(Screen.AppSelection.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToIntel = {
                    navController.navigate(Screen.Intel.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal.route)
                }
            )
        }

        composable(Screen.Intel.route) {
            IntelScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AppSelection.route) {
            val viewModel = remember { AppSelectionViewModel(database) }
            AppSelectionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Stats.route) {
            val context = LocalContext.current
            val viewModel = remember { StatsViewModel(database) }
            LaunchedEffect(Unit) {
                viewModel.loadStats(context)
            }
            StatsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Journal.route) {
            JournalScreen(
                database = database,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
