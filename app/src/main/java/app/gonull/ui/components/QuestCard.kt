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
import app.gonull.data.quest.Quest
import app.gonull.data.quest.QuestContent
import app.gonull.ui.theme.*

@Composable
fun QuestCard(
    onQuestCompleted: (Quest) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentQuest by remember { mutableStateOf(QuestContent.getRandomQuest()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentQuest.category.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "REAL WORLD QUEST",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = GoNullGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentQuest.title,
                style = MaterialTheme.typography.titleMedium,
                color = GoNullWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentQuest.description,
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "~${currentQuest.durationMinutes} min",
                style = MaterialTheme.typography.labelSmall,
                color = GoNullGray.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { currentQuest = QuestContent.getRandomQuest() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoNullGray
                    )
                ) {
                    Text("Different quest")
                }

                Button(
                    onClick = { onQuestCompleted(currentQuest) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoNullGreen,
                        contentColor = GoNullBlack
                    )
                ) {
                    Text("I did it!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
