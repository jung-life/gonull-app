package app.gonull.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.FocusModeEntity
import app.gonull.data.local.entity.ImplementationIntentionEntity
import app.gonull.service.FocusModeManager
import app.gonull.ui.components.AnalogModeCard
import app.gonull.ui.components.GymModeCard
import app.gonull.ui.components.IntentionManagementSection
import app.gonull.ui.components.MeditationModeCard
import app.gonull.ui.theme.*
import app.gonull.util.Constants
import app.gonull.util.PreferenceHelper
import app.gonull.util.PermissionHelper
import app.gonull.util.DeviceAdminHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    database: AppDatabase,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusModeManager = remember { FocusModeManager.getInstance(context) }

    // Focus mode states
    var gymModeActive by remember { mutableStateOf(false) }
    var meditationModeActive by remember { mutableStateOf(false) }
    var analogModeActive by remember { mutableStateOf(false) }
    var gymRemainingTime by remember { mutableStateOf<Long?>(null) }
    var meditationRemainingTime by remember { mutableStateOf<Long?>(null) }
    var analogRemainingTime by remember { mutableStateOf<Long?>(null) }

    // Boredom training preference
    var boredomEnabled by remember { mutableStateOf(PreferenceHelper.isBoredomBeforeUnlockEnabled(context)) }

    // Implementation intentions state
    var intentions by remember { mutableStateOf<List<ImplementationIntentionEntity>>(emptyList()) }

    // Load focus mode states and intentions
    LaunchedEffect(Unit) {
        intentions = database.implementationIntentionDao().getAllActiveIntentions()
        gymModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_GYM)
        meditationModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_MEDITATION)
        analogModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_ANALOG)
        gymRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_GYM)
        meditationRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_MEDITATION)
        analogRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_ANALOG)
    }

    // Periodically refresh focus mode states
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            gymModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_GYM)
            meditationModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_MEDITATION)
            analogModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_ANALOG)
            gymRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_GYM)
            meditationRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_MEDITATION)
            analogRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_ANALOG)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = GoNullWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        containerColor = GoNullBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                PermissionCard(
                    title = "Accessibility Service",
                    description = "Required to detect blocked app launches",
                    isGranted = PermissionHelper.isAccessibilityServiceEnabled(context),
                    onClick = { PermissionHelper.openAccessibilitySettings(context) }
                )
            }

            item {
                PermissionCard(
                    title = "Display Over Other Apps",
                    description = "Required to show blocking screen",
                    isGranted = PermissionHelper.canDrawOverlays(context),
                    onClick = { PermissionHelper.openOverlaySettings(context) }
                )
            }

            item {
                PermissionCard(
                    title = "Usage Access",
                    description = "Optional: For detailed usage stats",
                    isGranted = PermissionHelper.hasUsageStatsPermission(context),
                    onClick = { PermissionHelper.openUsageStatsSettings(context) }
                )
            }

            // Focus Modes Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Focus Modes",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                GymModeCard(
                    isActive = gymModeActive,
                    remainingTime = gymRemainingTime,
                    onToggle = { activate ->
                        scope.launch {
                            if (!activate) {
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_GYM)
                                gymModeActive = false
                                gymRemainingTime = null
                            }
                        }
                    },
                    onDurationSelect = { duration ->
                        scope.launch {
                            focusModeManager.activateFocusMode(FocusModeEntity.TYPE_GYM, duration)
                            gymModeActive = true
                            gymRemainingTime = duration?.let { it * 60 * 1000L }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                MeditationModeCard(
                    isActive = meditationModeActive,
                    remainingTime = meditationRemainingTime,
                    onToggle = { activate ->
                        scope.launch {
                            if (!activate) {
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_MEDITATION)
                                meditationModeActive = false
                                meditationRemainingTime = null
                            }
                        }
                    },
                    onDurationSelect = { duration ->
                        scope.launch {
                            focusModeManager.activateFocusMode(FocusModeEntity.TYPE_MEDITATION, duration)
                            meditationModeActive = true
                            meditationRemainingTime = duration?.let { it * 60 * 1000L }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                AnalogModeCard(
                    isActive = analogModeActive,
                    remainingTime = analogRemainingTime,
                    onToggle = { activate ->
                        scope.launch {
                            if (!activate) {
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_ANALOG)
                                analogModeActive = false
                                analogRemainingTime = null
                            }
                        }
                    },
                    onDurationSelect = { duration ->
                        scope.launch {
                            focusModeManager.activateFocusMode(FocusModeEntity.TYPE_ANALOG, duration)
                            analogModeActive = true
                            analogRemainingTime = duration?.let { it * 60 * 1000L }
                        }
                    }
                )
            }

            // Implementation Intentions Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                IntentionManagementSection(
                    intentions = intentions,
                    onAddIntention = { action ->
                        scope.launch {
                            database.implementationIntentionDao().insertIntention(
                                ImplementationIntentionEntity(thenAction = action)
                            )
                            intentions = database.implementationIntentionDao().getAllActiveIntentions()
                        }
                    },
                    onDeleteIntention = { intention ->
                        scope.launch {
                            database.implementationIntentionDao().deleteIntention(intention)
                            intentions = database.implementationIntentionDao().getAllActiveIntentions()
                        }
                    }
                )
            }

            // Boredom Training Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Blocking Behavior",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                BoredomTrainingToggleCard(
                    isEnabled = boredomEnabled,
                    onToggle = { enabled ->
                        boredomEnabled = enabled
                        PreferenceHelper.setBoredomBeforeUnlock(context, enabled)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Lock Mode (Removal Cooldown)",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                LockModeCard()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GoNullSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "GoNull",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoNullGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "An app blocker that uses commitment contracts to help you build healthier digital habits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGreen,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_URL))
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Send beta feedback",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGreen,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BETA_FEEDBACK_URL))
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockModeCard() {
    val context = LocalContext.current

    var isLockModeEnabled by remember { mutableStateOf(DeviceAdminHelper.isDeviceAdminEnabled(context)) }
    var removalRequestedAt by remember { mutableStateOf(PreferenceHelper.getRemovalRequestedAt(context)) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var showEnableDialog by remember { mutableStateOf(false) }
    var showCooldownDialog by remember { mutableStateOf(false) }

    val cooldownMillis = Constants.REMOVAL_COOLDOWN_MINUTES * 60_000L

    // Keep device-admin status, the pending removal request, and the clock in sync.
    LaunchedEffect(Unit) {
        while (true) {
            isLockModeEnabled = DeviceAdminHelper.isDeviceAdminEnabled(context)
            removalRequestedAt = PreferenceHelper.getRemovalRequestedAt(context)
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    // A pending removal only makes sense while Lock Mode is on; clear stale ones
    // (e.g. if the user disabled Device Admin manually in system settings).
    LaunchedEffect(isLockModeEnabled) {
        if (!isLockModeEnabled && removalRequestedAt > 0L) {
            PreferenceHelper.clearRemovalRequest(context)
            removalRequestedAt = 0L
        }
    }

    val cooldownActive = removalRequestedAt > 0L
    val remainingMillis =
        if (cooldownActive) (removalRequestedAt + cooldownMillis - now).coerceAtLeast(0L) else 0L
    val cooldownReady = cooldownActive && remainingMillis == 0L

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLockModeEnabled) GoNullRed.copy(alpha = 0.1f) else GoNullSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            !isLockModeEnabled -> "Inactive"
                            cooldownReady -> "Cooldown complete — you can remove GoNull"
                            cooldownActive -> "Turning off in ${formatCountdown(remainingMillis)}"
                            else -> "Active — removal goes through a cooldown"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLockModeEnabled) GoNullRed else GoNullGray
                    )
                }

                Switch(
                    checked = isLockModeEnabled,
                    // Locked during a running cooldown — use the explicit buttons below.
                    enabled = !cooldownActive,
                    onCheckedChange = {
                        if (it) showEnableDialog = true else showCooldownDialog = true
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoNullRed,
                        checkedTrackColor = GoNullRed.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cooldownActive) {
                if (cooldownReady) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                DeviceAdminHelper.removeDeviceAdmin(context)
                                PreferenceHelper.clearRemovalRequest(context)
                                removalRequestedAt = 0L
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Turn Off Lock Mode", color = GoNullGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                PreferenceHelper.clearRemovalRequest(context)
                                DeviceAdminHelper.startUninstall(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoNullRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Remove GoNull", color = GoNullWhite)
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            PreferenceHelper.clearRemovalRequest(context)
                            removalRequestedAt = 0L
                        }
                    ) {
                        Text("Cancel — keep GoNull locked", color = GoNullGreen)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Lock Mode adds friction so you can't quit on impulse. While it's on, removing " +
                        "GoNull starts a ${Constants.REMOVAL_COOLDOWN_MINUTES}-minute cooldown first. " +
                        "It's a speed bump, not a hard lock — you can cancel anytime, and you can always " +
                        "turn off Device Admin yourself in Settings > Security.",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }
    }

    if (showEnableDialog) {
        AlertDialog(
            onDismissRequest = { showEnableDialog = false },
            containerColor = GoNullSurface,
            title = { Text("Enable Lock Mode?", color = GoNullWhite) },
            text = {
                Column {
                    Text(
                        "Lock Mode uses Device Admin so removing GoNull goes through a " +
                                "${Constants.REMOVAL_COOLDOWN_MINUTES}-minute cooldown instead of an impulse tap.",
                        color = GoNullGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This is a commitment aid, not a trap — you can cancel the cooldown, and you can " +
                                "always disable Device Admin yourself in system Settings.",
                        color = GoNullYellow
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEnableDialog = false
                    DeviceAdminHelper.requestDeviceAdmin(context)
                }) {
                    Text("Enable Lock Mode", color = GoNullRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnableDialog = false }) {
                    Text("Cancel", color = GoNullGray)
                }
            }
        )
    }

    if (showCooldownDialog) {
        AlertDialog(
            onDismissRequest = { showCooldownDialog = false },
            containerColor = GoNullSurface,
            title = { Text("Start removal cooldown?", color = GoNullWhite) },
            text = {
                Text(
                    "Turning off Lock Mode starts a ${Constants.REMOVAL_COOLDOWN_MINUTES}-minute cooldown. " +
                            "When it ends you can remove GoNull or keep using it unlocked. " +
                            "You can cancel the cooldown at any time.",
                    color = GoNullGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCooldownDialog = false
                    val timestamp = System.currentTimeMillis()
                    PreferenceHelper.setRemovalRequestedAt(context, timestamp)
                    removalRequestedAt = timestamp
                }) {
                    Text("Start Cooldown", color = GoNullRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCooldownDialog = false }) {
                    Text("Keep Locked", color = GoNullGray)
                }
            }
        )
    }
}

private fun formatCountdown(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun BoredomTrainingToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) GoNullYellow.copy(alpha = 0.1f) else GoNullSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Boredom Training",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnabled) "Required before unlocking apps" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled) GoNullYellow else GoNullGray
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoNullYellow,
                        checkedTrackColor = GoNullYellow.copy(alpha = 0.5f),
                        uncheckedThumbColor = GoNullGray,
                        uncheckedTrackColor = GoNullBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "When enabled, you must sit through 2 minutes of boredom before the unlock timer starts. This trains your brain to tolerate discomfort without reaching for a screen.",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) GoNullGreen.copy(alpha = 0.1f) else GoNullSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }

            Text(
                text = if (isGranted) "Granted" else "Not granted",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) GoNullGreen else GoNullRed
            )
        }
    }
}
