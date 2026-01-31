package app.gonull.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.gonull.data.intel.IntelItem
import app.gonull.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyIntelCard(
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Get intel based on current day of year (same intel all day)
    val calendar = Calendar.getInstance()
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val intel = app.gonull.data.intel.IntelContent.getDailyIntel(dayOfYear)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GoNullSurface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Intel",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoNullGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = intel.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullWhite
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, intel.title)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${intel.title}\n\n${intel.body}\n\n- GoNull App"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Intel"))
                    }
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = GoNullGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body text
            Text(
                text = intel.body,
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row with category badge and "See all" link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    color = GoNullGreen.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = intel.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoNullGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // See all link
                if (onSeeAll != null) {
                    TextButton(onClick = onSeeAll) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoNullGreen
                        )
                    }
                }
            }
        }
    }
}
