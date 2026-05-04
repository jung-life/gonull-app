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
                    "Lock Mode (Anti-Uninstall)",
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
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Check device admin status periodically
    LaunchedEffect(Unit) {
        while (true) {
            isLockModeEnabled = DeviceAdminHelper.isDeviceAdminEnabled(context)
            kotlinx.coroutines.delay(1000)
        }
    }

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
                        text = if (isLockModeEnabled) "ACTIVE - App cannot be uninstalled" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLockModeEnabled) GoNullRed else GoNullGray
                    )
                }

                Switch(
                    checked = isLockModeEnabled,
                    onCheckedChange = {
                        if (it) {
                            showConfirmDialog = true
                        } else {
                            DeviceAdminHelper.openDeviceAdminSettings(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoNullRed,
                        checkedTrackColor = GoNullRed.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⚠️ When enabled, you cannot uninstall GoNull even if you try. " +
                        "This prevents you from giving up during moments of weakness. " +
                        "To disable, go to Settings > Security > Device Admins.",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = GoNullSurface,
            title = {
                Text("Enable Lock Mode?", color = GoNullWhite)
            },
            text = {
                Column {
                    Text(
                        "Lock Mode uses Device Admin privileges to prevent you from uninstalling GoNull during active block sessions.",
                        color = GoNullGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This is a serious commitment tool. You'll need to manually disable Device Admin in Settings to remove the app.",
                        color = GoNullYellow
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    DeviceAdminHelper.requestDeviceAdmin(context)
                }) {
                    Text("Enable Lock Mode", color = GoNullRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = GoNullGray)
                }
            }
        )
    }
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
