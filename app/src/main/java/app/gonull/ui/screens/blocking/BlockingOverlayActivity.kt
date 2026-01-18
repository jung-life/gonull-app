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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.service.UnlockTimerService
import app.gonull.ui.theme.*
import app.gonull.util.Constants
import kotlinx.coroutines.delay
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
            database.usageLogDao().insertLog(
                app.gonull.data.local.entity.UsageLogEntity(
                    packageName = packageName,
                    eventType = "EMERGENCY_UNLOCK"
                )
            )

            val request = UnlockRequestEntity(
                packageName = packageName,
                requestedAt = System.currentTimeMillis(),
                unlocksAt = System.currentTimeMillis(),
                status = Constants.RequestStatus.UNLOCKED,
                accessDurationMinutes = 15
            )

            database.unlockRequestDao().insertRequest(request)
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
    var showDelayDialog by remember { mutableStateOf(false) }
    var showBunkerMode by remember { mutableStateOf(false) }

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
            Text(
                text = "Ø",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 72.sp,
                    color = GoNullGreen
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                color = GoNullWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Is this app serving your long-term goals?",
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { showDelayDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullSurface,
                    contentColor = GoNullWhite
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start 30min Access Timer")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showBunkerMode = true }) {
                Text(
                    text = "Emergency Access (Bunker Mode)",
                    color = GoNullRed
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGoBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullGreen,
                    contentColor = GoNullBlack
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actually, I have things to do", fontWeight = FontWeight.Bold)
            }
        }

        if (showDelayDialog) {
            DelayVerificationDialog(
                onDismiss = { showDelayDialog = false },
                onVerified = {
                    showDelayDialog = false
                    onRequestUnlock()
                }
            )
        }

        if (showBunkerMode) {
            BunkerModeDialog(
                onDismiss = { showBunkerMode = false },
                onUnlock = {
                    showBunkerMode = false
                    onEmergencyUnlock()
                }
            )
        }
    }
}

@Composable
fun DelayVerificationDialog(
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = { Text("Are you sure?", color = GoNullWhite) },
        text = {
            Column {
                when(step) {
                    0 -> Text("The timer will take 30 minutes to complete. During this time, the app remains blocked.", color = GoNullGray)
                    1 -> Text("Research shows the 'itch' to check social media lasts about 15 minutes. Can you wait?", color = GoNullGray)
                    2 -> Text("Commitment: I am intentionally starting a 30-minute delay to access this app.", color = GoNullWhite, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (step < 2) step++ else onVerified()
            }) {
                Text(if (step < 2) "Next" else "Confirm & Start", color = GoNullGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoNullGray)
            }
        }
    )
}

@Composable
fun BunkerModeDialog(
    onDismiss: () -> Unit,
    onUnlock: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var inputCode by remember { mutableStateOf("") }
    val requiredCode = remember { (1000..9999).random().toString() }
    
    // Cognitive Friction Task
    var mathProblem by remember { mutableStateOf(generateMathProblem()) }
    var mathAnswer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = { Text("Bunker Mode Access", color = GoNullRed) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when(step) {
                    0 -> {
                        Text("Bunker Mode bypasses the timer but requires high cognitive effort to prevent impulsive use.", color = GoNullGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Type this random code to proceed:", color = GoNullWhite)
                        Text(requiredCode, style = MaterialTheme.typography.headlineMedium, color = GoNullYellow)
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GoNullWhite, unfocusedTextColor = GoNullWhite)
                        )
                    }
                    1 -> {
                        Text("Final Barrier: Solve this to prove your prefrontal cortex is active, not your impulsive limbic system.", color = GoNullGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(mathProblem.first, style = MaterialTheme.typography.headlineMedium, color = GoNullYellow)
                        OutlinedTextField(
                            value = mathAnswer,
                            onValueChange = { mathAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GoNullWhite, unfocusedTextColor = GoNullWhite)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (step == 0 && inputCode == requiredCode) {
                    step++
                } else if (step == 1 && mathAnswer == mathProblem.second) {
                    onUnlock()
                }
            }) {
                Text("Verify", color = GoNullRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("I'll wait for the timer", color = GoNullGray)
            }
        }
    )
}

fun generateMathProblem(): Pair<String, String> {
    val a = (10..50).random()
    val b = (10..50).random()
    val c = (2..9).random()
    return "($a + $b) * $c = ?" to ((a + b) * c).toString()
}
