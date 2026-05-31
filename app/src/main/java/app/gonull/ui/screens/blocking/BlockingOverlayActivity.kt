package app.gonull.ui.screens.blocking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
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
import app.gonull.data.local.entity.BoredomSessionEntity
import app.gonull.data.local.entity.QuestLogEntity
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.data.quest.Quest
import app.gonull.service.AccessExpirationService
import app.gonull.service.ProgressiveFrictionManager
import app.gonull.service.UnlockTimerService
import app.gonull.service.AccountabilityNotificationService
import app.gonull.service.UsageBudgetManager
import app.gonull.data.local.entity.TriggerLogEntity
import app.gonull.ui.components.BoredomTrainingScreen
import app.gonull.ui.components.CountdownTimer
import app.gonull.ui.components.ImplementationIntentionCard
import app.gonull.ui.components.LockedUntilTomorrow
import app.gonull.ui.components.QuestCard
import app.gonull.ui.components.ReflectionInput
import app.gonull.ui.components.TriggerCravingInterstitial
import app.gonull.ui.components.UsageMirrorCompact
import app.gonull.ui.components.StreakBreakWarning
import app.gonull.util.DateHelper
import app.gonull.util.PreferenceHelper
import app.gonull.ui.theme.*
import app.gonull.util.Constants
import kotlinx.coroutines.launch

class BlockingOverlayActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var frictionManager: ProgressiveFrictionManager
    private lateinit var budgetManager: UsageBudgetManager
    private lateinit var notificationService: AccountabilityNotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(applicationContext)
        frictionManager = ProgressiveFrictionManager.getInstance(applicationContext)
        budgetManager = UsageBudgetManager.getInstance(applicationContext)
        notificationService = AccountabilityNotificationService.getInstance(applicationContext)

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

        val boredomRequired = PreferenceHelper.isBoredomBeforeUnlockEnabled(this)

        setContent {
            GoNullTheme {
                var frictionLevel by remember { mutableIntStateOf(1) }
                var isLocked by remember { mutableStateOf(false) }
                var isBudgetExhausted by remember { mutableStateOf(false) }
                var remainingBudget by remember { mutableStateOf<Int?>(null) }
                var timeUntilReset by remember { mutableStateOf("") }
                var todayUsageMinutes by remember { mutableIntStateOf(0) }
                var weekUsageMinutes by remember { mutableIntStateOf(0) }
                var currentStreak by remember { mutableIntStateOf(0) }
                var intentionAction by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(packageName) {
                    frictionLevel = frictionManager.getNextFrictionLevel(packageName)
                    isLocked = frictionManager.isLockedForToday(packageName)
                    timeUntilReset = frictionManager.getFormattedTimeUntilReset()
                    isBudgetExhausted = budgetManager.isBudgetExhausted(packageName)
                    remainingBudget = budgetManager.getRemainingBudgetMinutes(packageName)

                    // Load usage stats
                    val today = DateHelper.getTodayString()
                    val weekAgo = DateHelper.getDateString(-7)
                    todayUsageMinutes = database.dailyUsageDao().getUsedMinutes(packageName, today) ?: 0
                    weekUsageMinutes = database.dailyUsageDao().getUsageForDateRange(packageName, weekAgo, today) ?: 0

                    // Load streak
                    val streak = database.streakDao().getStreak(packageName)
                    currentStreak = streak?.currentStreak ?: 0

                    // Load implementation intention
                    val intention = database.implementationIntentionDao().getIntentionForApp(packageName)
                    intentionAction = intention?.thenAction
                }

                BlockingScreen(
                    appName = appName,
                    packageName = packageName,
                    frictionLevel = frictionLevel,
                    isLocked = isLocked,
                    isBudgetExhausted = isBudgetExhausted,
                    remainingBudget = remainingBudget,
                    timeUntilReset = timeUntilReset,
                    todayUsageMinutes = todayUsageMinutes,
                    weekUsageMinutes = weekUsageMinutes,
                    currentStreak = currentStreak,
                    boredomRequired = boredomRequired,
                    intentionAction = intentionAction,
                    onTriggerRecorded = { trigger, craving ->
                        logTrigger(packageName, trigger, craving)
                    },
                    onRequestUnlock = { requestUnlock(packageName) },
                    onEmergencyUnlock = { emergencyUnlock(packageName) },
                    onBypassWithReflection = { reflection -> bypassWithReflection(packageName, reflection) },
                    onBypassComplete = { recordBypassAndUnlock(packageName) },
                    onQuestCompleted = { quest -> logQuestCompletion(quest, packageName) },
                    onBoredomCompleted = { durationSeconds -> logBoredomSession(packageName, durationSeconds, true) },
                    onBoredomCancelled = { durationSeconds -> logBoredomSession(packageName, durationSeconds, false) },
                    onGoBack = { goHome() }
                )
            }
        }

        // Back gesture/button never dismisses the block — it sends the user home.
        onBackPressedDispatcher.addCallback(this) {
            goHome()
        }
    }

    private fun logTrigger(packageName: String, trigger: String, cravingIntensity: Int) {
        lifecycleScope.launch {
            database.triggerLogDao().insertTrigger(
                TriggerLogEntity(
                    packageName = packageName,
                    trigger = trigger,
                    cravingIntensity = cravingIntensity
                )
            )
        }
    }

    private fun logQuestCompletion(quest: Quest, packageName: String) {
        lifecycleScope.launch {
            database.questLogDao().insertQuest(
                QuestLogEntity(
                    questId = quest.id,
                    questTitle = quest.title,
                    questCategory = quest.category.name,
                    triggeredByPackage = packageName
                )
            )
        }
    }

    private fun logBoredomSession(packageName: String, durationSeconds: Int, wasCompleted: Boolean) {
        lifecycleScope.launch {
            val session = BoredomSessionEntity(
                startedAt = System.currentTimeMillis() - (durationSeconds * 1000L),
                completedAt = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                wasCompleted = wasCompleted,
                triggeredByPackage = packageName
            )
            database.boredomSessionDao().insertSession(session)
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

            // Notify accountability partner
            notificationService.notifyEmergencyBypass(packageName)

            // Start expiration monitoring service
            AccessExpirationService.start(applicationContext)

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

    private fun bypassWithReflection(packageName: String, reflection: String) {
        val scope = lifecycleScope
        scope.launch {
            frictionManager.recordBypass(packageName, reflection)

            val request = UnlockRequestEntity(
                packageName = packageName,
                requestedAt = System.currentTimeMillis(),
                unlocksAt = System.currentTimeMillis(),
                status = Constants.RequestStatus.UNLOCKED,
                accessDurationMinutes = 15
            )

            database.unlockRequestDao().insertRequest(request)

            // Check if this triggers multiple bypass notification
            notificationService.checkAndNotifyMultipleBypasses(packageName)

            // Start expiration monitoring service
            AccessExpirationService.start(applicationContext)

            goHome()
        }
    }

    private fun recordBypassAndUnlock(packageName: String) {
        val scope = lifecycleScope
        scope.launch {
            frictionManager.recordBypass(packageName)

            val request = UnlockRequestEntity(
                packageName = packageName,
                requestedAt = System.currentTimeMillis(),
                unlocksAt = System.currentTimeMillis(),
                status = Constants.RequestStatus.UNLOCKED,
                accessDurationMinutes = 15
            )

            database.unlockRequestDao().insertRequest(request)

            // Check if this triggers multiple bypass notification
            notificationService.checkAndNotifyMultipleBypasses(packageName)

            // Start expiration monitoring service
            AccessExpirationService.start(applicationContext)

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

}

enum class BypassStep {
    TRIGGER_INTERSTITIAL,
    MAIN,
    DELAY_VERIFICATION,
    BUNKER_MODE,
    WAIT_COUNTDOWN,
    REFLECTION,
    BOREDOM_TRAINING,
    LOCKED
}

@Composable
fun BlockingScreen(
    appName: String,
    packageName: String,
    frictionLevel: Int,
    isLocked: Boolean,
    isBudgetExhausted: Boolean,
    remainingBudget: Int?,
    timeUntilReset: String,
    todayUsageMinutes: Int,
    weekUsageMinutes: Int,
    currentStreak: Int,
    boredomRequired: Boolean = false,
    intentionAction: String? = null,
    onTriggerRecorded: (String, Int) -> Unit = { _, _ -> },
    onRequestUnlock: () -> Unit,
    onEmergencyUnlock: () -> Unit,
    onBypassWithReflection: (String) -> Unit,
    onBypassComplete: () -> Unit,
    onQuestCompleted: (Quest) -> Unit,
    onBoredomCompleted: (Int) -> Unit,
    onBoredomCancelled: (Int) -> Unit,
    onGoBack: () -> Unit
) {
    // Show trigger interstitial first, unless locked or budget exhausted
    val initialStep = if (!isLocked && !isBudgetExhausted) BypassStep.TRIGGER_INTERSTITIAL else BypassStep.MAIN
    var currentStep by remember { mutableStateOf(initialStep) }
    var boredomStartTime by remember { mutableLongStateOf(0L) }

    // If locked or budget exhausted, show appropriate screen
    LaunchedEffect(isLocked, isBudgetExhausted) {
        if (isLocked) {
            currentStep = BypassStep.LOCKED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack),
        contentAlignment = Alignment.Center
    ) {
        when {
            currentStep == BypassStep.TRIGGER_INTERSTITIAL -> {
                TriggerCravingInterstitial(
                    appName = appName,
                    onComplete = { trigger, craving ->
                        onTriggerRecorded(trigger, craving)
                        currentStep = BypassStep.MAIN
                    },
                    onSkip = { currentStep = BypassStep.MAIN }
                )
            }
            isBudgetExhausted -> {
                BudgetExhaustedScreen(
                    appName = appName,
                    onGoBack = onGoBack
                )
            }
            currentStep == BypassStep.LOCKED -> {
                LockedScreen(
                    appName = appName,
                    timeUntilReset = timeUntilReset,
                    onGoBack = onGoBack
                )
            }
            currentStep == BypassStep.BOREDOM_TRAINING -> {
                BoredomTrainingScreen(
                    durationSeconds = 120,
                    onComplete = {
                        val elapsed = ((System.currentTimeMillis() - boredomStartTime) / 1000).toInt()
                        onBoredomCompleted(elapsed)
                        // After boredom training, continue to the unlock flow
                        currentStep = BypassStep.DELAY_VERIFICATION
                    },
                    onGiveUp = {
                        val elapsed = ((System.currentTimeMillis() - boredomStartTime) / 1000).toInt()
                        onBoredomCancelled(elapsed)
                        currentStep = BypassStep.MAIN
                    }
                )
            }
            currentStep == BypassStep.WAIT_COUNTDOWN -> {
                WaitCountdownScreen(
                    frictionLevel = frictionLevel,
                    onComplete = {
                        if (frictionLevel == Constants.Friction.LEVEL_3_BYPASS) {
                            currentStep = BypassStep.REFLECTION
                        } else {
                            onBypassComplete()
                        }
                    },
                    onCancel = { currentStep = BypassStep.MAIN }
                )
            }
            currentStep == BypassStep.REFLECTION -> {
                ReflectionScreen(
                    onSubmit = { reflection ->
                        onBypassWithReflection(reflection)
                    },
                    onCancel = { currentStep = BypassStep.MAIN }
                )
            }
            currentStep == BypassStep.DELAY_VERIFICATION -> {
                DelayVerificationDialog(
                    onDismiss = { currentStep = BypassStep.MAIN },
                    onVerified = {
                        currentStep = BypassStep.MAIN
                        onRequestUnlock()
                    }
                )
            }
            currentStep == BypassStep.BUNKER_MODE -> {
                BunkerModeDialog(
                    frictionLevel = frictionLevel,
                    onDismiss = { currentStep = BypassStep.MAIN },
                    onVerified = {
                        when (frictionLevel) {
                            Constants.Friction.LEVEL_1_BYPASS -> onBypassComplete()
                            Constants.Friction.LEVEL_2_BYPASS, Constants.Friction.LEVEL_3_BYPASS -> {
                                currentStep = BypassStep.WAIT_COUNTDOWN
                            }
                            else -> currentStep = BypassStep.LOCKED
                        }
                    }
                )
            }
            else -> {
                MainBlockingScreen(
                    appName = appName,
                    frictionLevel = frictionLevel,
                    remainingBudget = remainingBudget,
                    todayUsageMinutes = todayUsageMinutes,
                    weekUsageMinutes = weekUsageMinutes,
                    currentStreak = currentStreak,
                    boredomRequired = boredomRequired,
                    intentionAction = intentionAction,
                    onStartTimer = {
                        if (boredomRequired) {
                            boredomStartTime = System.currentTimeMillis()
                            currentStep = BypassStep.BOREDOM_TRAINING
                        } else {
                            currentStep = BypassStep.DELAY_VERIFICATION
                        }
                    },
                    onStartBoredomTraining = {
                        boredomStartTime = System.currentTimeMillis()
                        currentStep = BypassStep.BOREDOM_TRAINING
                    },
                    onEmergencyAccess = { currentStep = BypassStep.BUNKER_MODE },
                    onQuestCompleted = onQuestCompleted,
                    onGoBack = onGoBack
                )
            }
        }
    }
}

@Composable
fun MainBlockingScreen(
    appName: String,
    frictionLevel: Int,
    remainingBudget: Int?,
    todayUsageMinutes: Int,
    weekUsageMinutes: Int,
    currentStreak: Int,
    boredomRequired: Boolean = false,
    intentionAction: String? = null,
    onStartTimer: () -> Unit,
    onStartBoredomTraining: () -> Unit,
    onEmergencyAccess: () -> Unit,
    onQuestCompleted: (Quest) -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "\u00D8",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 72.sp,
                color = GoNullGreen
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium,
            color = GoNullWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (intentionAction != null) {
            ImplementationIntentionCard(
                thenAction = intentionAction,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Is this app serving your long-term goals?",
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Usage Mirror - shows time spent
        UsageMirrorCompact(
            todayMinutes = todayUsageMinutes,
            weekMinutes = weekUsageMinutes,
            modifier = Modifier.fillMaxWidth()
        )

        // Streak break warning (if user has a streak)
        if (currentStreak > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            StreakBreakWarning(
                currentStreak = currentStreak,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show remaining budget if applicable
        remainingBudget?.let { budget ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Budget remaining: ${budget}min",
                style = MaterialTheme.typography.bodyMedium,
                color = if (budget <= 5) GoNullRed else GoNullYellow,
                textAlign = TextAlign.Center
            )
        }

        // Show friction level indicator
        if (frictionLevel > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            FrictionLevelIndicator(level = frictionLevel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real World Quest Card
        QuestCard(
            onQuestCompleted = onQuestCompleted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Boredom Training button (always available as option)
        if (!boredomRequired) {
            OutlinedButton(
                onClick = onStartBoredomTraining,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoNullGray
                )
            ) {
                Text("Boredom Training (2 min)")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onStartTimer,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullSurface,
                contentColor = GoNullWhite
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (boredomRequired) "Start Boredom Training + Access Timer"
                else "Start 30min Access Timer"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onEmergencyAccess) {
            Text(
                text = "Emergency Access (Bunker Mode)",
                color = GoNullRed
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
}

@Composable
fun FrictionLevelIndicator(level: Int) {
    val description = when (level) {
        2 -> "Bypass #2: 5 min wait required"
        3 -> "Bypass #3: 15 min wait + reflection"
        4 -> "Locked until tomorrow"
        else -> ""
    }

    val color = when (level) {
        2 -> GoNullYellow
        3 -> GoNullRed.copy(alpha = 0.8f)
        4 -> GoNullRed
        else -> GoNullGray
    }

    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
fun WaitCountdownScreen(
    frictionLevel: Int,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val waitMinutes = when (frictionLevel) {
        Constants.Friction.LEVEL_2_BYPASS -> Constants.Friction.LEVEL_2_WAIT_MINUTES
        Constants.Friction.LEVEL_3_BYPASS -> Constants.Friction.LEVEL_3_WAIT_MINUTES
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mandatory Wait",
            style = MaterialTheme.typography.headlineSmall,
            color = GoNullYellow,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bypass #$frictionLevel requires a $waitMinutes minute wait",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CountdownTimer(
            totalSeconds = waitMinutes * 60,
            onComplete = onComplete
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onCancel,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GoNullGray
            )
        ) {
            Text("Cancel & Go Back")
        }
    }
}

@Composable
fun ReflectionScreen(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ReflectionInput(
            onSubmit = onSubmit,
            onCancel = onCancel
        )
    }
}

@Composable
fun LockedScreen(
    appName: String,
    timeUntilReset: String,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LockedUntilTomorrow(timeUntilReset = timeUntilReset)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Do something else", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BudgetExhaustedScreen(
    appName: String,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BUDGET EXHAUSTED",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GoNullRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = appName,
            style = MaterialTheme.typography.headlineSmall,
            color = GoNullWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You've used all your daily time budget for this app.",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No bypass options available.\nThis limit resets at midnight.",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Do something else", fontWeight = FontWeight.Bold)
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
    frictionLevel: Int,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var inputCode by remember { mutableStateOf("") }
    val requiredCode = remember { (1000..9999).random().toString() }

    var mathProblem by remember { mutableStateOf(generateMathProblem()) }
    var mathAnswer by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var mathError by remember { mutableStateOf(false) }

    val frictionDescription = when (frictionLevel) {
        Constants.Friction.LEVEL_2_BYPASS -> "After verification, you'll need to wait 5 minutes."
        Constants.Friction.LEVEL_3_BYPASS -> "After verification, you'll need to wait 15 minutes and write a reflection."
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = { Text("Bunker Mode Access", color = GoNullRed) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when(step) {
                    0 -> {
                        Text("Bunker Mode bypasses the timer but requires high cognitive effort to prevent impulsive use.", color = GoNullGray)
                        if (frictionDescription.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(frictionDescription, color = GoNullYellow, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Type this random code to proceed:", color = GoNullWhite)
                        Text(requiredCode, style = MaterialTheme.typography.headlineMedium, color = GoNullYellow)
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it
                                codeError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = codeError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoNullWhite,
                                unfocusedTextColor = GoNullWhite,
                                cursorColor = GoNullYellow,
                                focusedBorderColor = GoNullYellow,
                                unfocusedBorderColor = GoNullGray,
                                errorBorderColor = GoNullRed,
                                errorTextColor = GoNullWhite,
                                errorCursorColor = GoNullRed
                            )
                        )
                        if (codeError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Wrong code. Try again.",
                                color = GoNullRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    1 -> {
                        Text("Final Barrier: Solve this to prove your prefrontal cortex is active, not your impulsive limbic system.", color = GoNullGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(mathProblem.first, style = MaterialTheme.typography.headlineMedium, color = GoNullYellow)
                        OutlinedTextField(
                            value = mathAnswer,
                            onValueChange = {
                                mathAnswer = it
                                mathError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = mathError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoNullWhite,
                                unfocusedTextColor = GoNullWhite,
                                cursorColor = GoNullYellow,
                                focusedBorderColor = GoNullYellow,
                                unfocusedBorderColor = GoNullGray,
                                errorBorderColor = GoNullRed,
                                errorTextColor = GoNullWhite,
                                errorCursorColor = GoNullRed
                            )
                        )
                        if (mathError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Wrong answer. Try again.",
                                color = GoNullRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = {
                            mathProblem = generateMathProblem()
                            mathAnswer = ""
                            mathError = false
                        }) {
                            Text("Different question", color = GoNullYellow)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (step == 0) {
                    if (inputCode == requiredCode) {
                        codeError = false
                        step++
                    } else {
                        codeError = true
                    }
                } else if (step == 1) {
                    if (mathAnswer == mathProblem.second) {
                        mathError = false
                        onVerified()
                    } else {
                        mathError = true
                    }
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
