package app.gonull.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import app.gonull.data.AppDataCache
import app.gonull.data.local.AppDatabase
import app.gonull.ui.navigation.NavGraph
import app.gonull.ui.navigation.Screen
import app.gonull.ui.theme.GoNullTheme
import app.gonull.util.PermissionHelper
import app.gonull.util.PreferenceHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            GoNullTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(database = database)
                }
            }
        }
    }
}

@Composable
fun MainContent(database: AppDatabase) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()

    // Track permission state reactively
    var hasAllPermissions by remember { mutableStateOf(false) }
    var hasCompletedSetup by remember { mutableStateOf(false) }

    // Function to check all permissions
    fun checkPermissions() {
        hasAllPermissions = PermissionHelper.isAccessibilityServiceEnabled(context) &&
                PermissionHelper.canDrawOverlays(context) &&
                PermissionHelper.hasUsageStatsPermission(context)
        hasCompletedSetup = PreferenceHelper.isInitialSetupComplete(context)
    }

    // Check permissions on every resume (when returning from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial check
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // Preload app data when permissions are granted
    LaunchedEffect(hasAllPermissions) {
        if (hasAllPermissions) {
            // Start preloading app data in the background
            launch {
                AppDataCache.preload(context)
            }
        }
    }

    // Determine start destination
    val startDestination = when {
        !hasAllPermissions -> Screen.Onboarding.route
        !hasCompletedSetup -> Screen.UsageInsights.route
        else -> Screen.Home.route
    }

    // Navigate when permissions change
    LaunchedEffect(hasAllPermissions, hasCompletedSetup) {
        val currentRoute = navController.currentDestination?.route
        val targetRoute = when {
            !hasAllPermissions -> Screen.Onboarding.route
            !hasCompletedSetup -> Screen.UsageInsights.route
            else -> Screen.Home.route
        }

        // Only navigate if we're on onboarding and permissions are now granted
        if (currentRoute == Screen.Onboarding.route && hasAllPermissions) {
            if (!hasCompletedSetup) {
                navController.navigate(Screen.UsageInsights.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }
    }

    NavGraph(
        navController = navController,
        database = database,
        startDestination = startDestination
    )
}
