package app.gonull.ui.screens.appselection

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import app.gonull.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context.packageManager)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Apps to Block", color = GoNullWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullWhite
                        )
                    }
                },
                actions = {
                    if (selectedApps.isNotEmpty()) {
                        TextButton(onClick = {
                            viewModel.saveSelectedApps(context.packageManager)
                            onNavigateBack()
                        }) {
                            Text(
                                "Save (${selectedApps.size})",
                                color = GoNullGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        containerColor = GoNullBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search apps...", color = GoNullGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoNullGreen,
                    unfocusedBorderColor = GoNullBorder,
                    focusedTextColor = GoNullWhite,
                    unfocusedTextColor = GoNullWhite,
                    cursorColor = GoNullGreen
                ),
                singleLine = true
            )

            // App list
            LazyColumn {
                items(
                    items = installedApps.filter { appInfo ->
                        if (searchQuery.isEmpty()) true
                        else {
                            val appName = context.packageManager.getApplicationLabel(appInfo).toString()
                            appName.contains(searchQuery, ignoreCase = true) ||
                                    appInfo.packageName.contains(searchQuery, ignoreCase = true)
                        }
                    },
                    key = { it.packageName }
                ) { appInfo ->
                    val isSelected = selectedApps.contains(appInfo.packageName)

                    AppListItem(
                        appInfo = appInfo,
                        packageManager = context.packageManager,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleAppSelection(appInfo.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    appInfo: ApplicationInfo,
    packageManager: PackageManager,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val appName = remember { packageManager.getApplicationLabel(appInfo).toString() }
    val appIcon: Drawable = remember { packageManager.getApplicationIcon(appInfo) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) GoNullSurface else GoNullBlack)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        Image(
            bitmap = appIcon.toBitmap(width = 96, height = 96).asImageBitmap(),
            contentDescription = appName,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // App name and package
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullWhite
            )
            Text(
                text = appInfo.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = GoNullGray
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = GoNullGreen
            )
        }
    }
}
