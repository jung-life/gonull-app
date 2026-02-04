package app.gonull.ui.screens.warning

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.gonull.service.AccessExpirationService
import app.gonull.ui.components.AccessExpiringWarning
import app.gonull.ui.theme.GoNullTheme

/**
 * Full-screen activity that shows when app access is about to expire.
 * Displays a 2-minute countdown warning.
 */
class AccessWarningActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(AccessExpirationService.EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }

        val remainingMs = intent.getLongExtra(AccessExpirationService.EXTRA_REMAINING_MS, 120_000L)

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }

        setContent {
            GoNullTheme {
                AccessExpiringWarning(
                    appName = appName,
                    remainingMs = remainingMs,
                    onDismiss = {
                        AccessExpirationService.dismissWarning(applicationContext, packageName)
                        finish()
                    }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent dismissing with back button - user must acknowledge
        // Or allow it but mark as dismissed
        val packageName = intent.getStringExtra(AccessExpirationService.EXTRA_PACKAGE_NAME)
        packageName?.let {
            AccessExpirationService.dismissWarning(applicationContext, it)
        }
        finish()
    }
}
