package app.gonull.ui.screens.intel

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.gonull.data.intel.IntelContent
import app.gonull.data.intel.IntelItem
import app.gonull.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntelScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Intel Library",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GoNullGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        containerColor = GoNullBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Understanding why we block",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(IntelContent.allIntel) { intel ->
                IntelItemCard(intel = intel)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun IntelItemCard(intel: IntelItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GoNullSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = intel.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite,
                    modifier = Modifier.weight(1f)
                )

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = intel.body,
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )

            Spacer(modifier = Modifier.height(12.dp))

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
        }
    }
}
