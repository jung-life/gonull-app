package app.gonull.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

@Composable
fun MorningCommitmentCard(
    existingCommitment: String?,
    onSaveCommitment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (existingCommitment != null)
                GoNullGreen.copy(alpha = 0.1f)
            else
                GoNullYellow.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (existingCommitment != null) {
                // Completed state
                Text(
                    text = "Today's Intention",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = GoNullGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = existingCommitment,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullWhite,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            } else {
                // Input state
                Text(
                    text = "What's your intention today?",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullYellow,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set a daily commitment to stay focused",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("e.g., Stay present with my family tonight", color = GoNullGray.copy(alpha = 0.5f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GoNullWhite,
                        unfocusedTextColor = GoNullWhite,
                        cursorColor = GoNullYellow,
                        focusedBorderColor = GoNullYellow,
                        unfocusedBorderColor = GoNullBorder
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSaveCommitment(inputText.trim())
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoNullYellow,
                        contentColor = GoNullBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Commit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
