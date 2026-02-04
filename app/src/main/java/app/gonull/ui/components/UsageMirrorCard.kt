package app.gonull.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

/**
 * Usage mirror component that shows users their time spent on blocked apps.
 * This creates self-awareness and can deter mindless usage.
 */
@Composable
fun UsageMirrorCard(
    todayMinutes: Int,
    weekMinutes: Int,
    appName: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = GoNullSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                text = "YOUR TIME",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp
                ),
                color = GoNullGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Today's usage
                UsageStatColumn(
                    value = formatMinutes(todayMinutes),
                    label = "Today",
                    color = getUsageColor(todayMinutes, isDaily = true)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(GoNullBorder)
                )

                // This week's usage
                UsageStatColumn(
                    value = formatMinutes(weekMinutes),
                    label = "This Week",
                    color = getUsageColor(weekMinutes, isDaily = false)
                )
            }

            // Motivational message based on usage
            Spacer(modifier = Modifier.height(12.dp))

            val message = getMotivationalMessage(todayMinutes, weekMinutes)
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UsageStatColumn(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray
        )
    }
}

/**
 * Compact usage display for the blocking screen
 */
@Composable
fun UsageMirrorCompact(
    todayMinutes: Int,
    weekMinutes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = GoNullSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Time spent",
                style = MaterialTheme.typography.labelSmall,
                color = GoNullGray
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UsageChip(
                value = formatMinutes(todayMinutes),
                label = "today",
                color = getUsageColor(todayMinutes, isDaily = true)
            )
            UsageChip(
                value = formatMinutes(weekMinutes),
                label = "week",
                color = getUsageColor(weekMinutes, isDaily = false)
            )
        }
    }
}

@Composable
private fun UsageChip(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GoNullGray
        )
    }
}

private fun formatMinutes(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins == 0) "${hours}h" else "${hours}h ${mins}m"
        }
        else -> {
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            "${days}d ${hours}h"
        }
    }
}

private fun getUsageColor(minutes: Int, isDaily: Boolean): androidx.compose.ui.graphics.Color {
    // Thresholds based on whether it's daily or weekly
    val (low, medium, high) = if (isDaily) {
        Triple(30, 60, 120) // 30min, 1hr, 2hr for daily
    } else {
        Triple(120, 300, 600) // 2hr, 5hr, 10hr for weekly
    }

    return when {
        minutes <= low -> GoNullGreen
        minutes <= medium -> GoNullYellow
        else -> GoNullRed
    }
}

private fun getMotivationalMessage(todayMinutes: Int, weekMinutes: Int): String {
    return when {
        todayMinutes == 0 && weekMinutes == 0 -> "Clean slate. Keep it that way."
        todayMinutes == 0 -> "No usage today. Strong start."
        todayMinutes < 15 -> "Only ${todayMinutes} minutes today. Mindful usage."
        todayMinutes < 30 -> "Getting close to 30 minutes. Worth continuing?"
        todayMinutes < 60 -> "Almost an hour today. Is it adding value?"
        todayMinutes < 120 -> "${todayMinutes / 60}+ hours today. That's ${todayMinutes / 15} chapters of a book."
        else -> "${todayMinutes / 60} hours today. Time you won't get back."
    }
}
