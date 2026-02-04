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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Full-screen warning overlay shown when app access is about to expire.
 * Shows a 2-minute countdown with circular progress.
 */
@Composable
fun AccessExpiringWarning(
    appName: String,
    remainingMs: Long,
    onDismiss: () -> Unit,
    onExtend: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalWarningMs = 2 * 60 * 1000L // 2 minutes
    var currentRemainingMs by remember { mutableLongStateOf(remainingMs) }
    val progress = (currentRemainingMs.toFloat() / totalWarningMs).coerceIn(0f, 1f)

    // Countdown timer
    LaunchedEffect(Unit) {
        while (currentRemainingMs > 0) {
            delay(1000L)
            currentRemainingMs = (currentRemainingMs - 1000).coerceAtLeast(0)
        }
    }

    // Pulsing animation for urgency
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GoNullBlack.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Warning icon/text
            Text(
                text = "ACCESS ENDING",
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 4.sp
                ),
                color = GoNullYellow.copy(alpha = pulseAlpha),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall,
                color = GoNullWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Circular countdown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Track
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(180.dp),
                    color = GoNullSurface,
                    strokeWidth = 10.dp,
                    trackColor = GoNullSurface,
                    strokeCap = StrokeCap.Round
                )

                // Progress (counting down)
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(180.dp),
                    color = when {
                        currentRemainingMs <= 30_000 -> GoNullRed // Last 30 seconds
                        currentRemainingMs <= 60_000 -> GoNullYellow.copy(alpha = pulseAlpha) // Last minute
                        else -> GoNullYellow
                    },
                    strokeWidth = 10.dp,
                    trackColor = GoNullSurface,
                    strokeCap = StrokeCap.Round
                )

                // Time display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(currentRemainingMs),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            currentRemainingMs <= 30_000 -> GoNullRed
                            else -> GoNullWhite
                        }
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Message
            Text(
                text = "Save your progress and finish up",
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )

            Text(
                text = "App will be blocked when time runs out",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Dismiss button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullSurface,
                    contentColor = GoNullWhite
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got it, I'll wrap up")
            }

            // Optional extend button (could be used for accountability partner approved extension)
            onExtend?.let { extend ->
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = extend) {
                    Text(
                        text = "Request more time",
                        color = GoNullGray
                    )
                }
            }
        }
    }
}

/**
 * Compact warning card that can be shown as an overlay on top of the app
 */
@Composable
fun AccessExpiringBanner(
    appName: String,
    remainingMs: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentRemainingMs by remember { mutableLongStateOf(remainingMs) }

    LaunchedEffect(Unit) {
        while (currentRemainingMs > 0) {
            delay(1000L)
            currentRemainingMs = (currentRemainingMs - 1000).coerceAtLeast(0)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = GoNullSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                val bannerProgress = (currentRemainingMs.toFloat() / (2 * 60 * 1000L)).coerceIn(0f, 1f)
                CircularProgressIndicator(
                    progress = bannerProgress,
                    modifier = Modifier.size(48.dp),
                    color = if (currentRemainingMs <= 30_000) GoNullRed else GoNullYellow,
                    strokeWidth = 4.dp,
                    trackColor = GoNullBorder,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = formatTimeShort(currentRemainingMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Access ending soon",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoNullYellow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wrap up your session",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }

            TextButton(onClick = onDismiss) {
                Text("OK", color = GoNullGreen)
            }
        }
    }
}

private fun formatTime(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatTimeShort(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m" else "${seconds}s"
}
