package app.gonull.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    totalSeconds: Int,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()

    LaunchedEffect(key1 = remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else {
            onComplete()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(200.dp),
                color = GoNullGreen,
                trackColor = GoNullSurface,
                strokeWidth = 8.dp,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(remainingSeconds),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoNullWhite
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Take this time to reconsider.",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Is this app serving your goals?",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun LockedUntilTomorrow(
    timeUntilReset: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LOCKED",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = GoNullRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You've used all your bypasses today.",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Resets in $timeUntilReset",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "This limit exists to protect you from yourself.\nUse this time for something meaningful.",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
