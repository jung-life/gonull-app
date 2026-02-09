package app.gonull.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.data.local.entity.FocusModeEntity
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.service.FocusModeManager
import app.gonull.data.local.entity.JournalEntryEntity
import app.gonull.ui.components.DailyIntelCard
import app.gonull.ui.components.JournalPromptDialog
import app.gonull.ui.components.QuickFocusModeRow
import app.gonull.ui.components.RecentJournalCard
import app.gonull.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    database: AppDatabase,
    onNavigateToAppSelection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToIntel: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToJournal: () -> Unit
) {
    val blockedApps by viewModel.blockedApps.collectAsState()
    val pendingUnlocks by viewModel.pendingUnlocks.collectAsState()

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
    var showGymDurationPicker by remember { mutableStateOf(false) }
    var showMeditationDurationPicker by remember { mutableStateOf(false) }
    var showAnalogDurationPicker by remember { mutableStateOf(false) }

    // Journal prompt state (shown when a focus mode ends)
    var showJournalPrompt by remember { mutableStateOf(false) }
    var journalFocusModeName by remember { mutableStateOf("") }
    var journalFocusDuration by remember { mutableStateOf<Int?>(null) }

    // Journal state
    var journalTotalEntries by remember { mutableIntStateOf(0) }
    var journalLatestEntry by remember { mutableStateOf<String?>(null) }
    var journalLatestMood by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val entries = database.journalEntryDao().getRecentEntries(1)
        journalTotalEntries = database.journalEntryDao().getTotalEntries()
        if (entries.isNotEmpty()) {
            journalLatestEntry = entries[0].entryText
            journalLatestMood = entries[0].mood
        }
    }

    // Load and refresh focus mode states, detect expired modes
    LaunchedEffect(Unit) {
        while (true) {
            val wasGymActive = gymModeActive
            val wasMeditationActive = meditationModeActive
            val wasAnalogActive = analogModeActive

            gymModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_GYM)
            meditationModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_MEDITATION)
            analogModeActive = focusModeManager.isModeActive(FocusModeEntity.TYPE_ANALOG)
            gymRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_GYM)
            meditationRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_MEDITATION)
            analogRemainingTime = focusModeManager.getRemainingTime(FocusModeEntity.TYPE_ANALOG)

            // Detect expired modes and show journal prompt
            if (!showJournalPrompt) {
                when {
                    wasGymActive && !gymModeActive -> {
                        journalFocusModeName = "Gym Mode"
                        journalFocusDuration = null
                        showJournalPrompt = true
                    }
                    wasMeditationActive && !meditationModeActive -> {
                        journalFocusModeName = "Meditation Mode"
                        journalFocusDuration = null
                        showJournalPrompt = true
                    }
                    wasAnalogActive && !analogModeActive -> {
                        journalFocusModeName = "Analog Mode"
                        journalFocusDuration = null
                        showJournalPrompt = true
                    }
                }
            }

            kotlinx.coroutines.delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GoNull",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GoNullGreen
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = GoNullGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAppSelection,
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add app")
            }
        },
        containerColor = GoNullBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Focus Mode Quick Toggles
            item {
                QuickFocusModeRow(
                    isGymActive = gymModeActive,
                    isMeditationActive = meditationModeActive,
                    isAnalogActive = analogModeActive,
                    gymRemainingTime = gymRemainingTime,
                    meditationRemainingTime = meditationRemainingTime,
                    analogRemainingTime = analogRemainingTime,
                    onGymClick = {
                        if (gymModeActive) {
                            scope.launch {
                                val activatedAt = focusModeManager.getActivatedAt(FocusModeEntity.TYPE_GYM)
                                val durationMin = activatedAt?.let { ((System.currentTimeMillis() - it) / 60000).toInt() }
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_GYM)
                                gymModeActive = false
                                gymRemainingTime = null
                                journalFocusModeName = "Gym Mode"
                                journalFocusDuration = durationMin
                                showJournalPrompt = true
                            }
                        } else {
                            showGymDurationPicker = true
                        }
                    },
                    onMeditationClick = {
                        if (meditationModeActive) {
                            scope.launch {
                                val activatedAt = focusModeManager.getActivatedAt(FocusModeEntity.TYPE_MEDITATION)
                                val durationMin = activatedAt?.let { ((System.currentTimeMillis() - it) / 60000).toInt() }
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_MEDITATION)
                                meditationModeActive = false
                                meditationRemainingTime = null
                                journalFocusModeName = "Meditation Mode"
                                journalFocusDuration = durationMin
                                showJournalPrompt = true
                            }
                        } else {
                            showMeditationDurationPicker = true
                        }
                    },
                    onAnalogClick = {
                        if (analogModeActive) {
                            scope.launch {
                                val activatedAt = focusModeManager.getActivatedAt(FocusModeEntity.TYPE_ANALOG)
                                val durationMin = activatedAt?.let { ((System.currentTimeMillis() - it) / 60000).toInt() }
                                focusModeManager.deactivateFocusMode(FocusModeEntity.TYPE_ANALOG)
                                analogModeActive = false
                                analogRemainingTime = null
                                journalFocusModeName = "Analog Mode"
                                journalFocusDuration = durationMin
                                showJournalPrompt = true
                            }
                        } else {
                            showAnalogDurationPicker = true
                        }
                    }
                )
            }

            // Stats summary (tappable to see full stats)
            item {
                StatsCard(
                    blockedToday = viewModel.blockedCountToday,
                    timeSavedMinutes = viewModel.timeSavedMinutes,
                    onTap = onNavigateToStats
                )
            }

            // Daily Intel
            item {
                DailyIntelCard(onSeeAll = onNavigateToIntel)
            }

            // Reclaim Journal
            item {
                RecentJournalCard(
                    latestEntry = journalLatestEntry,
                    latestMood = journalLatestMood,
                    totalEntries = journalTotalEntries,
                    onClick = onNavigateToJournal
                )
            }

            // Pending unlocks
            if (pendingUnlocks.isNotEmpty()) {
                item {
                    Text(
                        "Pending Unlocks",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullGray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(pendingUnlocks) { unlock ->
                    PendingUnlockCard(
                        unlock = unlock,
                        onCancel = { viewModel.cancelUnlockRequest(unlock) }
                    )
                }
            }

            // Blocked apps
            item {
                Text(
                    "Blocked Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (blockedApps.isEmpty()) {
                item {
                    EmptyState(onAddApp = onNavigateToAppSelection)
                }
            } else {
                items(blockedApps) { app ->
                    BlockedAppCard(
                        app = app,
                        onRemove = { viewModel.removeBlockedApp(app) }
                    )
                }
            }
        }
    }

    // Gym Mode Duration Picker
    if (showGymDurationPicker) {
        FocusModeDurationDialog(
            title = "Start Gym Mode",
            emoji = "💪",
            options = listOf(
                30 to "30 minutes",
                60 to "1 hour",
                90 to "1.5 hours",
                120 to "2 hours",
                null to "Until I stop"
            ),
            onDurationSelect = { duration ->
                showGymDurationPicker = false
                scope.launch {
                    focusModeManager.activateFocusMode(FocusModeEntity.TYPE_GYM, duration)
                    gymModeActive = true
                    gymRemainingTime = duration?.let { it * 60 * 1000L }
                }
            },
            onDismiss = { showGymDurationPicker = false }
        )
    }

    // Meditation Mode Duration Picker
    if (showMeditationDurationPicker) {
        FocusModeDurationDialog(
            title = "Start Meditation Mode",
            emoji = "\uD83E\uDDD8",
            options = listOf(
                15 to "15 minutes",
                30 to "30 minutes",
                45 to "45 minutes",
                60 to "1 hour",
                null to "Until I stop"
            ),
            onDurationSelect = { duration ->
                showMeditationDurationPicker = false
                scope.launch {
                    focusModeManager.activateFocusMode(FocusModeEntity.TYPE_MEDITATION, duration)
                    meditationModeActive = true
                    meditationRemainingTime = duration?.let { it * 60 * 1000L }
                }
            },
            onDismiss = { showMeditationDurationPicker = false }
        )
    }

    // Analog Mode Duration Picker
    if (showAnalogDurationPicker) {
        FocusModeDurationDialog(
            title = "Start Analog Mode",
            emoji = "\uD83D\uDCDF",
            options = listOf(
                30 to "30 minutes",
                60 to "1 hour",
                120 to "2 hours",
                240 to "4 hours",
                null to "Until I stop"
            ),
            onDurationSelect = { duration ->
                showAnalogDurationPicker = false
                scope.launch {
                    focusModeManager.activateFocusMode(FocusModeEntity.TYPE_ANALOG, duration)
                    analogModeActive = true
                    analogRemainingTime = duration?.let { it * 60 * 1000L }
                }
            },
            onDismiss = { showAnalogDurationPicker = false }
        )
    }

    // Journal Prompt Dialog (shown when focus mode ends)
    if (showJournalPrompt) {
        JournalPromptDialog(
            focusModeName = journalFocusModeName,
            durationMinutes = journalFocusDuration,
            onSave = { result ->
                showJournalPrompt = false
                scope.launch {
                    database.journalEntryDao().insertEntry(
                        JournalEntryEntity(
                            entryText = result.entryText,
                            linkedFocusMode = journalFocusModeName,
                            focusDurationMinutes = journalFocusDuration,
                            mood = result.mood
                        )
                    )
                    // Refresh journal card data
                    val entries = database.journalEntryDao().getRecentEntries(1)
                    journalTotalEntries = database.journalEntryDao().getTotalEntries()
                    if (entries.isNotEmpty()) {
                        journalLatestEntry = entries[0].entryText
                        journalLatestMood = entries[0].mood
                    }
                }
            },
            onSkip = { showJournalPrompt = false }
        )
    }
}

@Composable
private fun FocusModeDurationDialog(
    title: String,
    emoji: String,
    options: List<Pair<Int?, String>>,
    onDurationSelect: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(text = emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, color = GoNullWhite)
            }
        },
        text = {
            Column {
                Text(
                    text = "Music apps will be allowed during this time",
                    color = GoNullGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { (duration, label) ->
                    TextButton(
                        onClick = { onDurationSelect(duration) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = label,
                            color = if (duration == null) GoNullGreen else GoNullWhite,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoNullGray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsCard(blockedToday: Int, timeSavedMinutes: Int, onTap: () -> Unit) {
    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Blocks stat
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = blockedToday.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GoNullGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "blocks",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GoNullGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Time saved stat
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatTimeSaved(timeSavedMinutes),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = GoNullYellow
                    )
                    Text(
                        text = "time saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
            }
        }
    }
}

private fun formatTimeSaved(minutes: Int): String {
    return when {
        minutes >= 60 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
        }
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}

@Composable
fun PendingUnlockCard(
    unlock: UnlockRequestEntity,
    onCancel: () -> Unit
) {
    val remainingMillis = unlock.unlocksAt - System.currentTimeMillis()
    val remainingMinutes = (remainingMillis / 60000).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unlock.packageName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unlocks in $remainingMinutes minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGreen
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = GoNullGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppCard(
    app: BlockedAppEntity,
    onRemove: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        onClick = { showConfirmDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${app.unlockDelayMinutes} min delay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = GoNullSurface,
            title = {
                Text("Unblock ${app.appName}?", color = GoNullWhite)
            },
            text = {
                Text(
                    "This will remove the app from your blocked list.",
                    color = GoNullGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onRemove()
                }) {
                    Text("Yes, unblock", color = GoNullRed)
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
fun EmptyState(onAddApp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ø",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 64.sp,
                color = GoNullGreen.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No apps blocked yet",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onAddApp) {
            Text("Add your first app", color = GoNullGreen)
        }
    }
}
