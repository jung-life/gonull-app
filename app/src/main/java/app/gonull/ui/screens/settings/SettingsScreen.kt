package app.gonull.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.gonull.ui.theme.*
import app.gonull.util.PermissionHelper
import app.gonull.util.DeviceAdminHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = GoNullWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullWhite
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                PermissionCard(
                    title = "Accessibility Service",
                    description = "Required to detect blocked app launches",
                    isGranted = PermissionHelper.isAccessibilityServiceEnabled(context),
                    onClick = { PermissionHelper.openAccessibilitySettings(context) }
                )
            }

            item {
                PermissionCard(
                    title = "Display Over Other Apps",
                    description = "Required to show blocking screen",
                    isGranted = PermissionHelper.canDrawOverlays(context),
                    onClick = { PermissionHelper.openOverlaySettings(context) }
                )
            }

            item {
                PermissionCard(
                    title = "Usage Access",
                    description = "Optional: For detailed usage stats",
                    isGranted = PermissionHelper.hasUsageStatsPermission(context),
                    onClick = { PermissionHelper.openUsageStatsSettings(context) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Lock Mode (Anti-Uninstall)",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                LockModeCard()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GoNullSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "GoNull",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoNullGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "An app blocker that uses commitment contracts to help you break social media addiction.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockModeCard() {
    val context = LocalContext.current
    var isLockModeEnabled by remember { mutableStateOf(DeviceAdminHelper.isDeviceAdminEnabled(context)) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Check device admin status periodically
    LaunchedEffect(Unit) {
        while (true) {
            isLockModeEnabled = DeviceAdminHelper.isDeviceAdminEnabled(context)
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLockModeEnabled) GoNullRed.copy(alpha = 0.1f) else GoNullSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoNullWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLockModeEnabled) "ACTIVE - App cannot be uninstalled" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLockModeEnabled) GoNullRed else GoNullGray
                    )
                </Column>

                Switch(
                    checked = isLockModeEnabled,
                    onCheckedChange = {
                        if (it) {
                            showConfirmDialog = true
                        } else {
                            DeviceAdminHelper.openDeviceAdminSettings(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoNullRed,
                        checkedTrackColor = GoNullRed.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⚠️ When enabled, you cannot uninstall GoNull even if you try. " +
                        "This prevents you from giving up during moments of weakness. " +
                        "To disable, go to Settings > Security > Device Admins.",
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = GoNullSurface,
            title = {
                Text("Enable Lock Mode?", color = GoNullWhite)
            },
            text = {
                Column {
                    Text(
                        "Lock Mode uses Device Admin privileges to prevent you from uninstalling GoNull during active block sessions.",
                        color = GoNullGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This is a serious commitment tool. You'll need to manually disable Device Admin in Settings to remove the app.",
                        color = GoNullYellow
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    DeviceAdminHelper.requestDeviceAdmin(context)
                }) {
                    Text("Enable Lock Mode", color = GoNullRed)
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
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) GoNullGreen.copy(alpha = 0.1f) else GoNullSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }

            Text(
                text = if (isGranted) "Granted" else "Not granted",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) GoNullGreen else GoNullRed
            )
        }
    }
}
