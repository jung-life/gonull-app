package app.gonull.ui.screens.blocking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.service.UnlockTimerService
import app.gonull.ui.theme.*
import app.gonull.util.Constants
import kotlinx.coroutines.launch

class BlockingOverlayActivity : ComponentActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(applicationContext)
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: run {
            finish()
            return
        }

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }

        setContent {
            GoNullTheme {
                BlockingScreen(
                    appName = appName,
                    packageName = packageName,
                    onRequestUnlock = { requestUnlock(packageName) },
                    onEmergencyUnlock = { emergencyUnlock(packageName) },
                    onGoBack = { goHome() }
                )
            }
        }
    }

    private fun emergencyUnlock(packageName: String) {
        val scope = lifecycleScope
        scope.launch {
            // Log emergency unlock for accountability
            database.usageLogDao().insertLog(
                app.gonull.data.local.entity.UsageLogEntity(
                    packageName = packageName,
                    eventType = "EMERGENCY_UNLOCK"
                )
            )

            // Grant immediate access by creating an already-unlocked request
            val request = UnlockRequestEntity(
                packageName = packageName,
                requestedAt = System.currentTimeMillis(),
                unlocksAt = System.currentTimeMillis(), // Already unlocked
                status = Constants.RequestStatus.UNLOCKED,
                accessDurationMinutes = 15
            )

            database.unlockRequestDao().insertRequest(request)

            // TODO: If financial stakes are enabled, charge penalty here

            goHome()
        }
    }

    private fun requestUnlock(packageName: String) {
        val scope = lifecycleScope
        scope.launch {
            val blockedApp = database.blockedAppDao().getBlockedApp(packageName)
            val delayMinutes = blockedApp?.unlockDelayMinutes ?: Constants.DEFAULT_UNLOCK_DELAY_MINUTES

            val request = UnlockRequestEntity(
                packageName = packageName,
                requestedAt = System.currentTimeMillis(),
                unlocksAt = System.currentTimeMillis() + (delayMinutes * 60 * 1000L),
                status = Constants.RequestStatus.PENDING
            )

            database.unlockRequestDao().insertRequest(request)

            // Start timer service
            UnlockTimerService.startTimer(
                context = applicationContext,
                packageName = packageName,
                unlockTime = request.unlocksAt
            )

            goHome()
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goHome()
    }
}

@Composable
fun BlockingScreen(
    appName: String,
    packageName: String,
    onRequestUnlock: () -> Unit,
    onEmergencyUnlock: () -> Unit,
    onGoBack: () -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }
    var showEmergencyUnlock by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Null symbol
            Text(
                text = "Ø",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 72.sp,
                    color = GoNullGreen
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                color = GoNullWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "is blocked",
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullGray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Request unlock button
            Button(
                onClick = { showConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullSurface,
                    contentColor = GoNullWhite
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Request Access")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency unlock button
            TextButton(onClick = { showEmergencyUnlock = true }) {
                Text(
                    text = "Emergency Access",
                    color = GoNullRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Go back button
            TextButton(onClick = onGoBack) {
                Text(
                    text = "Go Back",
                    color = GoNullGray
                )
            }
        }

        // Confirmation dialog
        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { showConfirmation = false },
                containerColor = GoNullSurface,
                title = {
                    Text(
                        "Request unlock?",
                        color = GoNullWhite
                    )
                },
                text = {
                    Text(
                        "You'll get access in 30 minutes. Is it worth the wait?",
                        color = GoNullGray
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmation = false
                        onRequestUnlock()
                    }) {
                        Text("Yes, start timer", color = GoNullGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmation = false }) {
                        Text("Never mind", color = GoNullGray)
                    }
                }
            )
        }

        // Emergency unlock dialog with friction
        if (showEmergencyUnlock) {
            EmergencyUnlockDialog(
                onDismiss = { showEmergencyUnlock = false },
                onUnlock = {
                    showEmergencyUnlock = false
                    onEmergencyUnlock()
                }
            )
        }
    }
}

@Composable
fun EmergencyUnlockDialog(
    onDismiss: () -> Unit,
    onUnlock: () -> Unit
) {
    var tapCount by remember { mutableStateOf(0) }
    val requiredTaps = 50

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = {
            Text(
                "⚠️ Emergency Unlock",
                color = GoNullRed
            )
        },
        text = {
            Column {
                Text(
                    "This should only be used for genuine emergencies.",
                    color = GoNullGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GoNullBlack
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Tap the button below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "$tapCount / $requiredTaps",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 48.sp
                            ),
                            color = if (tapCount >= requiredTaps) GoNullGreen else GoNullRed
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { tapCount++ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tapCount >= requiredTaps) GoNullGreen else GoNullRed,
                                contentColor = GoNullBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (tapCount >= requiredTaps) "Unlocked - Tap to Continue" else "TAP HERE",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                if (tapCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This emergency unlock will be logged for accountability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullYellow
                    )
                }
            }
        },
        confirmButton = {
            if (tapCount >= requiredTaps) {
                TextButton(onClick = onUnlock) {
                    Text("Continue to App", color = GoNullGreen)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoNullGray)
            }
        }
    )
}
