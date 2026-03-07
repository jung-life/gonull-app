package app.gonull.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.local.dao.TriggerCount
import app.gonull.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()
    val topBlockedApps by viewModel.topBlockedApps.collectAsState()
    val motivationalMessage by viewModel.motivationalMessage.collectAsState()
    val triggerCounts by viewModel.triggerCounts.collectAsState()
    val avgCraving by viewModel.avgCraving.collectAsState()
    val avgWorthIt by viewModel.avgWorthIt.collectAsState()
    val notWorthItCount by viewModel.notWorthItCount.collectAsState()
    val totalReflections by viewModel.totalReflections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Progress",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoNullWhite
                    )
                },
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
            // Motivational Card
            item {
                MotivationalCard(message = motivationalMessage)
            }

            // Main Stats
            item {
                StatsOverviewCard(stats = stats)
            }

            // Time Saved Highlight
            item {
                TimeSavedCard(
                    totalBlocks = stats.totalBlocks,
                    timeSavedMinutes = stats.timeSavedMinutes
                )
            }

            // Success Rate
            item {
                SuccessRateCard(
                    totalBlocks = stats.totalBlocks,
                    totalBypasses = stats.totalBypasses
                )
            }

            // Trigger Patterns
            if (triggerCounts.isNotEmpty()) {
                item {
                    TriggerPatternsCard(
                        triggerCounts = triggerCounts,
                        avgCraving = avgCraving
                    )
                }
            }

            // Session Insights
            if (totalReflections > 0) {
                item {
                    SessionInsightsCard(
                        avgWorthIt = avgWorthIt,
                        notWorthItCount = notWorthItCount,
                        totalReflections = totalReflections
                    )
                }
            }

            // Top Blocked Apps
            if (topBlockedApps.isNotEmpty()) {
                item {
                    Text(
                        text = "MOST BLOCKED APPS",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = GoNullGreen,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(topBlockedApps) { app ->
                    TopBlockedAppItem(
                        appName = app.appName,
                        blockCount = app.blockCount
                    )
                }
            }

            // Recent Activity
            if (recentActivity.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = GoNullGreen,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(recentActivity) { activity ->
                    ActivityItem(activity = activity)
                }
            }

            // Encouragement Footer
            item {
                EncouragementFooter()
            }
        }
    }
}

@Composable
fun MotivationalCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullGreen.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💪",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullWhite,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatsOverviewCard(stats: StatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = stats.totalBlocks.toString(),
                label = "Total\nBlocks",
                color = GoNullGreen
            )
            StatItem(
                value = stats.todayBlocks.toString(),
                label = "Today's\nBlocks",
                color = GoNullYellow
            )
            StatItem(
                value = stats.weekBlocks.toString(),
                label = "This\nWeek",
                color = GoNullWhite
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TimeSavedCard(totalBlocks: Int, timeSavedMinutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "TIME RECLAIMED",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = GoNullGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatTimeSaved(timeSavedMinutes),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoNullYellow
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "That's ${totalBlocks} moments you chose yourself over the algorithm.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )

            // What you could do with this time
            if (timeSavedMinutes >= 30) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = GoNullBorder)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WITH THIS TIME YOU COULD:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = GoNullGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                val activities = getActivitiesForTime(timeSavedMinutes)
                activities.forEach { activity ->
                    Text(
                        text = "• $activity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullWhite,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessRateCard(totalBlocks: Int, totalBypasses: Int) {
    val total = totalBlocks + totalBypasses
    val successRate = if (total > 0) (totalBlocks.toFloat() / total * 100).toInt() else 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WILLPOWER SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = GoNullGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalBlocks resisted • $totalBypasses bypassed",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
                Text(
                    text = "$successRate%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = when {
                        successRate >= 80 -> GoNullGreen
                        successRate >= 50 -> GoNullYellow
                        else -> GoNullRed
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(GoNullBlack, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(successRate / 100f)
                        .fillMaxHeight()
                        .background(
                            when {
                                successRate >= 80 -> GoNullGreen
                                successRate >= 50 -> GoNullYellow
                                else -> GoNullRed
                            },
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun TopBlockedAppItem(appName: String, blockCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullWhite
            )
            Text(
                text = "$blockCount blocks",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullYellow
            )
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityData) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (activity.type) {
                "BLOCKED" -> GoNullGreen.copy(alpha = 0.1f)
                "BYPASS" -> GoNullYellow.copy(alpha = 0.1f)
                else -> GoNullRed.copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (activity.type) {
                        "BLOCKED" -> "🛡️"
                        "BYPASS" -> "⚠️"
                        else -> "🚨"
                    },
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = activity.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullWhite
                    )
                    Text(
                        text = when (activity.type) {
                            "BLOCKED" -> "Blocked successfully"
                            "BYPASS" -> "Bypassed"
                            else -> "Emergency unlock"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeFormat.format(Date(activity.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
                Text(
                    text = dateFormat.format(Date(activity.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = GoNullGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EncouragementFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌱",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Live in the Real World",
                style = MaterialTheme.typography.titleMedium,
                color = GoNullWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Every block is a choice to be present. Go for a walk. Call a friend. Read a book. Cook a meal. Your life is happening now, not on a screen.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = GoNullBorder)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "\"The present moment is filled with joy and happiness. If you are attentive, you will see it.\"",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Text(
                text = "— Thich Nhat Hanh",
                style = MaterialTheme.typography.labelSmall,
                color = GoNullGreen,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TriggerPatternsCard(
    triggerCounts: List<TriggerCount>,
    avgCraving: Float?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "TRIGGER PATTERNS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = GoNullYellow
            )
            Spacer(modifier = Modifier.height(12.dp))

            triggerCounts.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.trigger.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullWhite
                    )
                    Text(
                        text = "${item.count}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            avgCraving?.let { avg ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = GoNullBorder)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Avg craving intensity: %.1f/5".format(avg),
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }
        }
    }
}

@Composable
fun SessionInsightsCard(
    avgWorthIt: Float?,
    notWorthItCount: Int,
    totalReflections: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "SESSION INSIGHTS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = GoNullYellow
            )
            Spacer(modifier = Modifier.height(12.dp))

            avgWorthIt?.let { avg ->
                Text(
                    text = "Avg \"worth it\" rating: %.1f/5".format(avg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (totalReflections > 0) {
                val notWorthPercent = (notWorthItCount.toFloat() / totalReflections * 100).toInt()
                Text(
                    text = "$notWorthPercent% of sessions weren't worth it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notWorthPercent >= 50) GoNullRed else GoNullYellow,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on $totalReflections reflections",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }
        }
    }
}

private fun formatTimeSaved(minutes: Int): String {
    return when {
        minutes >= 1440 -> { // 24 hours
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            if (hours > 0) "${days}d ${hours}h" else "${days}d"
        }
        minutes >= 60 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
        }
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}

private fun getActivitiesForTime(minutes: Int): List<String> {
    val activities = mutableListOf<String>()

    if (minutes >= 15) activities.add("Take a refreshing walk outside")
    if (minutes >= 20) activities.add("Have a meaningful conversation")
    if (minutes >= 30) activities.add("Read a chapter of a book")
    if (minutes >= 45) activities.add("Cook a healthy meal")
    if (minutes >= 60) activities.add("Learn something new")
    if (minutes >= 90) activities.add("Exercise and stretch")
    if (minutes >= 120) activities.add("Work on a personal project")

    return activities.take(3)
}

// Data classes
data class StatsData(
    val totalBlocks: Int = 0,
    val todayBlocks: Int = 0,
    val weekBlocks: Int = 0,
    val totalBypasses: Int = 0,
    val timeSavedMinutes: Int = 0
)

data class ActivityData(
    val appName: String,
    val type: String,
    val timestamp: Long
)

data class TopBlockedApp(
    val appName: String,
    val blockCount: Int
)
