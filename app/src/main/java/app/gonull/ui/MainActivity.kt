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
import app.gonull.service.BlockingStrategy
import app.gonull.service.BlockingStrategyManager
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

    // Resolve the start destination ONCE, synchronously, at first composition.
    // Permission checks are synchronous system-service queries, so the correct
    // entry point is known immediately for both new and returning users.
    //
    // Critically, this value must NOT be reactive. A startDestination that
    // recomputes when permissions change makes NavHost re-navigate to the new
    // start destination — which was yanking users off the onboarding permissions
    // screen the instant Accessibility + Usage Stats became granted (before they
    // reached Notifications or pressed "Let's Start Our Journey"). New users now
    // leave onboarding solely via the button (onComplete → NavGraph).
    val startDestination = remember {
        val accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
        val usageStatsEnabled = PermissionHelper.hasUsageStatsPermission(context)
        val setupDone = PreferenceHelper.isInitialSetupComplete(context)
        when {
            !(accessibilityEnabled && usageStatsEnabled) -> Screen.Onboarding.route
            !setupDone -> Screen.UsageInsights.route
            else -> Screen.Home.route
        }
    }

    // Track permissions for preload, blocking-strategy selection, and the
    // returning-user correction below — never as the driver of onboarding exit.
    var hasAllPermissions by remember { mutableStateOf(false) }
    var hasCompletedSetup by remember { mutableStateOf(false) }

    // Display Over Apps is optional — if unavailable (OEM-disabled on some
    // Vivo/Oppo devices), fall back to UsageStatsBased blocking (2-second lag).
    fun refreshPermissionState() {
        val accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
        val overlayEnabled = PermissionHelper.canDrawOverlays(context)
        val usageStatsEnabled = PermissionHelper.hasUsageStatsPermission(context)

        hasAllPermissions = accessibilityEnabled && usageStatsEnabled
        hasCompletedSetup = PreferenceHelper.isInitialSetupComplete(context)
        if (hasAllPermissions && !overlayEnabled) {
            BlockingStrategyManager(context).setStrategy(BlockingStrategy.UsageStatsBased)
        }
    }

    // Refresh on every resume (when returning from settings).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        refreshPermissionState()
    }

    // Preload app data once permissions are granted.
    LaunchedEffect(hasAllPermissions) {
        if (hasAllPermissions) {
            launch {
                AppDataCache.preload(context)
            }
        }
    }

    // Returning-user correction ONLY. If the one-time startDestination guessed
    // onboarding because the accessibility service hadn't re-bound at the first
    // frame, forward a user who has ALREADY completed setup to Home. Guarded by
    // hasCompletedSetup so new users (setup incomplete) are never auto-forwarded
    // when permissions flip true mid-onboarding — that was the original bug.
    LaunchedEffect(hasAllPermissions, hasCompletedSetup) {
        val currentRoute = navController.currentDestination?.route
        if (currentRoute == Screen.Onboarding.route && hasAllPermissions && hasCompletedSetup) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    NavGraph(
        navController = navController,
        database = database,
        startDestination = startDestination
    )
}
