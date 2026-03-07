package app.gonull.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

private enum class InterstitialStep { TRIGGER, CRAVING }

private val TRIGGERS = listOf(
    "BORED" to "Bored",
    "ANXIOUS" to "Anxious",
    "LONELY" to "Lonely",
    "PROCRASTINATING" to "Procrastinating",
    "AUTOPILOT" to "Autopilot"
)

@Composable
fun TriggerCravingInterstitial(
    appName: String,
    onComplete: (trigger: String, cravingIntensity: Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(InterstitialStep.TRIGGER) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    var selectedCraving by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            InterstitialStep.TRIGGER -> {
                Text(
                    text = "What triggered this?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You reached for $appName. What were you feeling?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                TRIGGERS.forEach { (key, label) ->
                    val isSelected = selectedTrigger == key
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) GoNullGreen.copy(alpha = 0.2f)
                                else GoNullSurface
                            )
                            .clickable { selectedTrigger = key }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) GoNullGreen else GoNullWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { step = InterstitialStep.CRAVING },
                    enabled = selectedTrigger != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoNullGreen,
                        contentColor = GoNullBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next", fontWeight = FontWeight.Bold)
                }
            }
            InterstitialStep.CRAVING -> {
                Text(
                    text = "How strong is the urge?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mild",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                    Text(
                        text = "Intense",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                RatingSelector(
                    selectedRating = selectedCraving,
                    onRatingSelected = { selectedCraving = it },
                    label = ""
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        selectedTrigger?.let { trigger ->
                            selectedCraving?.let { craving ->
                                onComplete(trigger, craving)
                            }
                        }
                    },
                    enabled = selectedCraving != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoNullGreen,
                        contentColor = GoNullBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text("Skip", color = GoNullGray)
        }
    }
}
