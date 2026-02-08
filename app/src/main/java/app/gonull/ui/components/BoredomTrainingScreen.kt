package app.gonull.ui.components

import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BoredomTrainingScreen(
    durationSeconds: Int = 120,
    onComplete: () -> Unit,
    onGiveUp: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(durationSeconds) }
    var isComplete by remember { mutableStateOf(false) }

    // Keep screen on
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        isComplete = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack),
        contentAlignment = Alignment.Center
    ) {
        if (isComplete) {
            // Completion screen
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "\u2705",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "You made it.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoNullGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Still want to open that app?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoNullGreen,
                        contentColor = GoNullBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Boredom screen - deliberately minimal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Faint countdown timer
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp
                    ),
                    color = GoNullGray.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sit with the boredom.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullGray.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This feeling is temporary. You are retraining your brain.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Barely visible give up button
                TextButton(onClick = onGiveUp) {
                    Text(
                        text = "Give up",
                        color = GoNullGray.copy(alpha = 0.2f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
