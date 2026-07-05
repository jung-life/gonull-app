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

    // Track permission state reactively
    var hasAllPermissions by remember { mutableStateOf(false) }
    var hasCompletedSetup by remember { mutableStateOf(false) }

    // Function to check all permissions. Display Over Apps is optional — if
    // unavailable (OEM-disabled on some Vivo/Oppo devices), we fall back to
    // UsageStatsBased blocking with a 2-second lag instead of instant.
    fun checkPermissions() {
        val accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
        val overlayEnabled = PermissionHelper.canDrawOverlays(context)
        val usageStatsEnabled = PermissionHelper.hasUsageStatsPermission(context)

        hasAllPermissions = accessibilityEnabled && usageStatsEnabled
        hasCompletedSetup = PreferenceHelper.isInitialSetupComplete(context)

        // If permissions are met but overlay is missing, switch to UsageStatsBased
        if (hasAllPermissions && !overlayEnabled) {
            val strategyManager = BlockingStrategyManager(context)
            strategyManager.setStrategy(BlockingStrategy.UsageStatsBased)
        }
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

    // Auto-skip onboarding ONLY for returning users who already finished setup.
    // A returning user can briefly land on the onboarding route because
    // `startDestination` is computed before `checkPermissions()` resolves the
    // real permission state on the first frame; this corrects that.
    //
    // We deliberately do NOT forward *new* users (hasCompletedSetup == false)
    // when permissions flip true mid-flow. During onboarding, granting
    // Accessibility + Usage Stats makes `hasAllPermissions` true before the user
    // has granted Notifications or pressed "Let's Start Our Journey" — auto-
    // navigating here was yanking them off onboarding right after Usage Stats.
    // New users leave onboarding solely via the button (onComplete → NavGraph).
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
