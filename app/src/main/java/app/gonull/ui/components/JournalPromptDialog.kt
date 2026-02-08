package app.gonull.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

data class JournalPromptResult(
    val entryText: String,
    val mood: String
)

@Composable
fun JournalPromptDialog(
    focusModeName: String,
    durationMinutes: Int?,
    onSave: (JournalPromptResult) -> Unit,
    onSkip: () -> Unit
) {
    var entryText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    val moods = listOf(
        "\uD83D\uDE0A" to "great",
        "\uD83D\uDE42" to "good",
        "\uD83D\uDE10" to "neutral",
        "\uD83D\uDE11" to "meh"
    )

    AlertDialog(
        onDismissRequest = onSkip,
        containerColor = GoNullSurface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDCD3",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "What did you do instead?",
                    color = GoNullWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (durationMinutes != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$focusModeName ended after ${durationMinutes}min",
                        color = GoNullGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        text = {
            Column {
                // Mood selector
                Text(
                    text = "How do you feel?",
                    color = GoNullGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { (emoji, mood) ->
                        Surface(
                            modifier = Modifier.clickable { selectedMood = mood },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedMood == mood) GoNullGreen.copy(alpha = 0.2f) else GoNullBlack
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = entryText,
                    onValueChange = { entryText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Went for a walk, read a book...", color = GoNullGray.copy(alpha = 0.5f)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GoNullWhite,
                        unfocusedTextColor = GoNullWhite,
                        cursorColor = GoNullGreen,
                        focusedBorderColor = GoNullGreen,
                        unfocusedBorderColor = GoNullBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(JournalPromptResult(
                        entryText = entryText,
                        mood = selectedMood ?: "neutral"
                    ))
                },
                enabled = entryText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullGreen,
                    contentColor = GoNullBlack
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Skip", color = GoNullGray)
            }
        }
    )
}
