package app.gonull.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentJournalCard(
    latestEntry: String?,
    latestMood: String?,
    totalEntries: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\uD83D\uDCD3",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reclaim Journal",
                        style = MaterialTheme.typography.titleSmall,
                        color = GoNullWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$totalEntries entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoNullGray
                )
            }

            if (latestEntry != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    if (latestMood != null) {
                        val moodEmoji = when (latestMood) {
                            "great" -> "\uD83D\uDE0A"
                            "good" -> "\uD83D\uDE42"
                            "neutral" -> "\uD83D\uDE10"
                            "meh" -> "\uD83D\uDE11"
                            else -> "\uD83D\uDE10"
                        }
                        Text(text = moodEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = latestEntry,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your journal entries will appear here after focus modes end",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}
