package app.gonull.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.gonull.ui.theme.*
import app.gonull.util.Constants

@Composable
fun ReflectionInput(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reflectionText by remember { mutableStateOf("") }
    val isValid = reflectionText.length >= Constants.Friction.MIN_REFLECTION_CHARS
    val charsRemaining = Constants.Friction.MIN_REFLECTION_CHARS - reflectionText.length

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reflection Required",
            style = MaterialTheme.typography.headlineSmall,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Before accessing this app for the third time today, take a moment to reflect:",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Why do you need to use this app right now?\nWhat will you accomplish?",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullYellow,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = reflectionText,
            onValueChange = { reflectionText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = {
                Text(
                    "Write your honest reflection here...",
                    color = GoNullGray.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GoNullWhite,
                unfocusedTextColor = GoNullWhite,
                cursorColor = GoNullGreen,
                focusedBorderColor = if (isValid) GoNullGreen else GoNullYellow,
                unfocusedBorderColor = GoNullGray
            ),
            supportingText = {
                Text(
                    text = if (charsRemaining > 0) {
                        "$charsRemaining more characters needed"
                    } else {
                        "Minimum reached"
                    },
                    color = if (isValid) GoNullGreen else GoNullGray
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoNullGray
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onSubmit(reflectionText) },
                enabled = isValid,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValid) GoNullGreen else GoNullGray,
                    contentColor = GoNullBlack
                )
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReflectionPrompts() {
    val prompts = listOf(
        "What specific task do you need this app for?",
        "Have you tried doing something else first?",
        "How will you feel after spending 30 minutes here?",
        "What important task are you avoiding?"
    )

    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "Consider these questions:",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        prompts.forEachIndexed { index, prompt ->
            Text(
                text = "${index + 1}. $prompt",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
