package app.gonull.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

/**
 * Quick toggle chips for Gym and Yoga modes on the home screen
 */
@Composable
fun QuickFocusModeRow(
    isGymActive: Boolean,
    isYogaActive: Boolean,
    gymRemainingTime: Long?,
    yogaRemainingTime: Long?,
    onGymClick: () -> Unit,
    onYogaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickModeChip(
            emoji = "💪",
            label = "Gym",
            isActive = isGymActive,
            remainingTime = gymRemainingTime,
            activeColor = GoNullGreen,
            onClick = onGymClick,
            modifier = Modifier.weight(1f)
        )
        QuickModeChip(
            emoji = "🧘",
            label = "Yoga",
            isActive = isYogaActive,
            remainingTime = yogaRemainingTime,
            activeColor = GoNullYellow,
            onClick = onYogaClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickModeChip(
    emoji: String,
    label: String,
    isActive: Boolean,
    remainingTime: Long?,
    activeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) activeColor.copy(alpha = 0.2f) else GoNullSurface
    val borderColor = if (isActive) activeColor else GoNullBorder

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) activeColor else GoNullWhite,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                AnimatedVisibility(visible = isActive && remainingTime != null) {
                    Text(
                        text = formatTime(remainingTime ?: 0),
                        style = MaterialTheme.typography.labelSmall,
                        color = activeColor.copy(alpha = 0.8f)
                    )
                }
                AnimatedVisibility(visible = isActive && remainingTime == null) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m left"
        minutes > 0 -> "${minutes}m left"
        else -> "< 1m left"
    }
}
