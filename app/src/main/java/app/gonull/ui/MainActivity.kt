package app.gonull.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import app.gonull.data.local.AppDatabase
import app.gonull.ui.navigation.NavGraph
import app.gonull.ui.navigation.Screen
import app.gonull.ui.theme.GoNullTheme
import app.gonull.util.PermissionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)

        // Determine start destination based on permissions
        val hasRequiredPermissions = PermissionHelper.isAccessibilityServiceEnabled(this) &&
                PermissionHelper.canDrawOverlays(this)

        val startDestination = if (hasRequiredPermissions) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            GoNullTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        database = database,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
