package app.gonull.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

/**
 * Streak counter component that shows consecutive days without using a blocked app.
 * Breaking a streak has psychological cost, making it a powerful deterrent.
 */
@Composable
fun StreakCounter(
    currentStreak: Int,
    longestStreak: Int,
    appName: String? = null,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for streaks > 0
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (currentStreak > 0) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (currentStreak > 0) {
                GoNullSurface
            } else {
                GoNullSurface.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fire emoji and streak number
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                if (currentStreak > 0) {
                    Text(
                        text = getStreakEmoji(currentStreak),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 36.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = currentStreak.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = getStreakColor(currentStreak)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (currentStreak == 1) "day" else "days",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Label
            Text(
                text = if (currentStreak > 0) "STREAK" else "NO STREAK",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp
                ),
                color = GoNullGray
            )

            // Longest streak indicator
            if (longestStreak > currentStreak && longestStreak > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Best: $longestStreak days",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGrayDark
                )
            }

            // Motivational message
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = getStreakMessage(currentStreak, longestStreak),
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Compact streak display for the blocking screen
 */
@Composable
fun StreakCounterCompact(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                brush = if (currentStreak > 0) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            getStreakColor(currentStreak).copy(alpha = 0.2f),
                            GoNullSurface.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(GoNullSurface.copy(alpha = 0.5f), GoNullSurface.copy(alpha = 0.5f))
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (currentStreak > 0) {
            Text(
                text = getStreakEmoji(currentStreak),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Text(
            text = "$currentStreak day${if (currentStreak != 1) "s" else ""} clean",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (currentStreak > 0) getStreakColor(currentStreak) else GoNullGray
        )
    }
}

/**
 * Warning shown on blocking screen when user is about to break a streak
 */
@Composable
fun StreakBreakWarning(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    if (currentStreak <= 0) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GoNullRed.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getStreakEmoji(currentStreak),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "You'll break your $currentStreak-day streak",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoNullRed
                )
                Text(
                    text = "Is it worth starting over?",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }
        }
    }
}

private fun getStreakEmoji(streak: Int): String {
    return when {
        streak >= 100 -> "\uD83D\uDC8E" // Diamond
        streak >= 50 -> "\uD83D\uDC51" // Crown
        streak >= 30 -> "\u2B50" // Star
        streak >= 14 -> "\uD83D\uDD25" // Fire
        streak >= 7 -> "\uD83D\uDCAA" // Flexed bicep
        streak >= 3 -> "\u2728" // Sparkles
        streak >= 1 -> "\u2705" // Check mark
        else -> ""
    }
}

private fun getStreakColor(streak: Int): Color {
    return when {
        streak >= 30 -> Color(0xFFFFD700) // Gold
        streak >= 14 -> Color(0xFFFF6B35) // Orange
        streak >= 7 -> GoNullGreen
        streak >= 1 -> GoNullGreen.copy(alpha = 0.8f)
        else -> GoNullGray
    }
}

private fun getStreakMessage(current: Int, longest: Int): String {
    return when {
        current == 0 && longest == 0 -> "Start your streak today"
        current == 0 && longest > 0 -> "You've done $longest days before. Do it again."
        current == 1 -> "Day 1. Every streak starts here."
        current < 7 -> "Building momentum. ${7 - current} more days to a week."
        current == 7 -> "One full week! You're proving something to yourself."
        current < 14 -> "Strong week. ${14 - current} days to two weeks."
        current == 14 -> "Two weeks strong. This is becoming a habit."
        current < 30 -> "${30 - current} days to a full month."
        current == 30 -> "30 days! You've rewired your brain."
        current < longest -> "${longest - current} more to beat your record."
        current == longest && longest > 0 -> "Tied with your best! Tomorrow sets a new record."
        else -> "New personal record! Keep pushing."
    }
}
