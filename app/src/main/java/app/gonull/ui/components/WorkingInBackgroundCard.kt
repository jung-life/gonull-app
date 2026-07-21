package app.gonull.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*
import app.gonull.util.PreferenceHelper

/**
 * Communicates GoNull's core "anti-pattern": we don't want you living inside
 * this app. Success is fewer taps here and less screen time out there, with the
 * blocker quietly working in the background. Once a week it expands into a gentle
 * reflection nudge so users periodically check how it's actually feeling for them.
 */
@Composable
fun WorkingInBackgroundCard(
    onReflect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weekMillis = 7L * 24 * 60 * 60 * 1000

    var lastPrompt by remember { mutableStateOf(PreferenceHelper.getLastReviewPromptAt(context)) }
    val now = remember { System.currentTimeMillis() }
    // Don't nag on a brand-new install: only prompt once a week has passed since
    // the first time this card recorded a baseline.
    val showWeeklyCheckIn = lastPrompt != 0L && (now - lastPrompt) >= weekMillis

    // Seed the baseline the first time the card is ever shown.
    LaunchedEffect(Unit) {
        if (lastPrompt == 0L) {
            PreferenceHelper.setLastReviewPromptAt(context, now)
            lastPrompt = now
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "WORKING IN THE BACKGROUND",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                color = GoNullGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "GoNull is doing its job even when you're not here. We don't want you " +
                        "living in this app — the win is fewer taps here and less time scrolling out there.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "More blocks + less screen time = it's working.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullWhite,
                fontWeight = FontWeight.Medium
            )

            if (showWeeklyCheckIn) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = GoNullBorder)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Weekly check-in",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "How is this actually feeling? Is your screen time going down — and are you " +
                            "reaching for your phone less on autopilot?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            PreferenceHelper.setLastReviewPromptAt(context, System.currentTimeMillis())
                            lastPrompt = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Not now", color = GoNullGray)
                    }
                    Button(
                        onClick = {
                            PreferenceHelper.setLastReviewPromptAt(context, System.currentTimeMillis())
                            lastPrompt = System.currentTimeMillis()
                            onReflect()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoNullGreen,
                            contentColor = GoNullBlack
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reflect", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
