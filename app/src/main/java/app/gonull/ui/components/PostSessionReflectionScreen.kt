package app.gonull.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.gonull.ui.theme.*

@Composable
fun PostSessionReflectionScreen(
    appName: String,
    sessionMinutes: Int?,
    onSubmit: (worthItRating: Int, reflectionText: String?) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var reflectionText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Was it worth it?",
            style = MaterialTheme.typography.headlineSmall,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your $appName session has ended" +
                    (sessionMinutes?.let { " ($it min)" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        RatingSelector(
            selectedRating = selectedRating,
            onRatingSelected = { selectedRating = it },
            label = "How worthwhile was that session?"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = reflectionText,
            onValueChange = { reflectionText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Any thoughts? (optional)", color = GoNullGray.copy(alpha = 0.5f))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GoNullWhite,
                unfocusedTextColor = GoNullWhite,
                cursorColor = GoNullGreen,
                focusedBorderColor = GoNullGreen,
                unfocusedBorderColor = GoNullBorder
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedRating?.let { rating ->
                    onSubmit(rating, reflectionText.ifBlank { null })
                }
            },
            enabled = selectedRating != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSkip) {
            Text("Skip", color = GoNullGray)
        }
    }
}
