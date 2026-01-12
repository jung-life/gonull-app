package app.gonull.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAppSelection: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val blockedApps by viewModel.blockedApps.collectAsState()
    val pendingUnlocks by viewModel.pendingUnlocks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GoNull",
                        fontFamily = JetBrainsMono,
                        color = GoNullGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = GoNullGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAppSelection,
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add app")
            }
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
            // Stats summary
            item {
                StatsCard(blockedToday = viewModel.blockedCountToday)
            }

            // Pending unlocks
            if (pendingUnlocks.isNotEmpty()) {
                item {
                    Text(
                        "Pending Unlocks",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullGray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(pendingUnlocks) { unlock ->
                    PendingUnlockCard(
                        unlock = unlock,
                        onCancel = { viewModel.cancelUnlockRequest(unlock) }
                    )
                }
            }

            // Blocked apps
            item {
                Text(
                    "Blocked Apps",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (blockedApps.isEmpty()) {
                item {
                    EmptyState(onAddApp = onNavigateToAppSelection)
                }
            } else {
                items(blockedApps) { app ->
                    BlockedAppCard(
                        app = app,
                        onRemove = { viewModel.removeBlockedApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(blockedToday: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = blockedToday.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoNullGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "blocks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PendingUnlockCard(
    unlock: UnlockRequestEntity,
    onCancel: () -> Unit
) {
    val remainingMillis = unlock.unlocksAt - System.currentTimeMillis()
    val remainingMinutes = (remainingMillis / 60000).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unlock.packageName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unlocks in $remainingMinutes minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGreen
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = GoNullGray
                )
            }
        }
    }
}

@Composable
fun BlockedAppCard(
    app: BlockedAppEntity,
    onRemove: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${app.unlockDelayMinutes} min delay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
            }
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = GoNullGray
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = GoNullSurface,
            title = {
                Text("Unblock ${app.appName}?", color = GoNullWhite)
            },
            text = {
                Text(
                    "This will remove the app from your blocked list.",
                    color = GoNullGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onRemove()
                }) {
                    Text("Yes, unblock", color = GoNullRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = GoNullGray)
                }
            }
        )
    }
}

@Composable
fun EmptyState(onAddApp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ø",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 64.sp,
                color = GoNullGreen.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No apps blocked yet",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onAddApp) {
            Text("Add your first app", color = GoNullGreen)
        }
    }
}
