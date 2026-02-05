package app.gonull.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.local.entity.FocusModeEntity
import app.gonull.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun GymModeCard(
    isActive: Boolean,
    remainingTime: Long?,
    onToggle: (Boolean) -> Unit,
    onDurationSelect: (Int?) -> Unit
) {
    FocusModeCard(
        title = "Gym Mode",
        emoji = "💪",
        description = "Music apps play without interruption during your workout",
        activeDescription = "Music apps won't be blocked",
        accentColor = GoNullGreen,
        isActive = isActive,
        remainingTime = remainingTime,
        onToggle = onToggle,
        onDurationSelect = onDurationSelect,
        durationOptions = listOf(
            30 to "30 min",
            60 to "1 hour",
            90 to "1.5 hours",
            120 to "2 hours",
            null to "Until I stop"
        )
    )
}

@Composable
fun YogaModeCard(
    isActive: Boolean,
    remainingTime: Long?,
    onToggle: (Boolean) -> Unit,
    onDurationSelect: (Int?) -> Unit
) {
    FocusModeCard(
        title = "Yoga & Meditation Mode",
        emoji = "🧘",
        description = "Calm music and meditation apps for peaceful practice",
        activeDescription = "Meditation & music apps allowed",
        accentColor = GoNullYellow,
        isActive = isActive,
        remainingTime = remainingTime,
        onToggle = onToggle,
        onDurationSelect = onDurationSelect,
        durationOptions = listOf(
            15 to "15 min",
            30 to "30 min",
            45 to "45 min",
            60 to "1 hour",
            null to "Until I stop"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeCard(
    title: String,
    emoji: String,
    description: String,
    activeDescription: String,
    accentColor: androidx.compose.ui.graphics.Color,
    isActive: Boolean,
    remainingTime: Long?,
    onToggle: (Boolean) -> Unit,
    onDurationSelect: (Int?) -> Unit,
    durationOptions: List<Pair<Int?, String>>
) {
    var showDurationPicker by remember { mutableStateOf(false) }
    var displayTime by remember { mutableStateOf(remainingTime) }

    // Update timer display
    LaunchedEffect(isActive, remainingTime) {
        displayTime = remainingTime
        if (isActive && remainingTime != null) {
            while (displayTime != null && displayTime!! > 0) {
                delay(1000)
                displayTime = displayTime?.minus(1000)?.coerceAtLeast(0)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) accentColor.copy(alpha = 0.15f) else GoNullSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoNullWhite,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedVisibility(visible = isActive) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(accentColor, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (displayTime != null) {
                                        "Active • ${formatRemainingTime(displayTime!!)}"
                                    } else {
                                        "Active"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { newValue ->
                        if (newValue) {
                            showDurationPicker = true
                        } else {
                            onToggle(false)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.5f),
                        uncheckedThumbColor = GoNullGray,
                        uncheckedTrackColor = GoNullBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isActive) activeDescription else description,
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }
    }

    // Duration picker dialog
    if (showDurationPicker) {
        AlertDialog(
            onDismissRequest = { showDurationPicker = false },
            containerColor = GoNullSurface,
            title = {
                Column {
                    Text(
                        text = emoji,
                        fontSize = 32.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start $title",
                        color = GoNullWhite,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "How long will you be working out?",
                        color = GoNullGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    durationOptions.forEach { (duration, label) ->
                        TextButton(
                            onClick = {
                                showDurationPicker = false
                                onDurationSelect(duration)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                color = if (duration == null) accentColor else GoNullWhite,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDurationPicker = false }) {
                    Text("Cancel", color = GoNullGray)
                }
            }
        )
    }
}

private fun formatRemainingTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
