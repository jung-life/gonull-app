package app.gonull.ui.screens.appselection

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.gonull.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val categorizedApps by viewModel.categorizedApps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val usageInfoMap by viewModel.usageInfoMap.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    val isSaving by viewModel.

    isSaving.collectAsState()

    // Load apps on initial composition
    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    // Refresh apps whenever the screen is resumed (e.g., returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadApps(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Apps to Block",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoNullWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullWhite
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveSelectedApps(context, context.packageManager, onNavigateBack)
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GoNullGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save (${selectedApps.size})",
                                color = if (selectedApps.isNotEmpty()) GoNullGreen else GoNullGray
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
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoNullGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoNullGreen,
                    unfocusedBorderColor = GoNullBorder,
                    focusedTextColor = GoNullWhite,
                    unfocusedTextColor = GoNullWhite,
                    cursorColor = GoNullGreen
                ),
                singleLine = true
            )

            if (!isLoaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ø",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 48.sp,
                                color = GoNullGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading apps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray
                        )
                    }
                }
            } else {
                LazyColumn {
                    categorizedApps.forEach { category ->
                        val filteredApps = category.apps.filter { appInfo ->
                            if (searchQuery.isEmpty()) true
                            else {
                                val appName = context.packageManager.getApplicationLabel(appInfo).toString()
                                appName.contains(searchQuery, ignoreCase = true) ||
                                        appInfo.packageName.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        if (filteredApps.isNotEmpty()) {
                            item {
                                CategoryHeader(category.name)
                            }

                            items(
                                items = filteredApps,
                                key = { "${category.name}_${it.packageName}" }
                            ) { appInfo ->
                                val isSelected = selectedApps.contains(appInfo.packageName)
                                val usageInfo = usageInfoMap[appInfo.packageName]

                                AppListItem(
                                    appInfo = appInfo,
                                    packageManager = context.packageManager,
                                    isSelected = isSelected,
                                    usageInfo = usageInfo,
                                    onClick = { viewModel.toggleAppSelection(appInfo.packageName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        color = GoNullGreen,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(GoNullSurface.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun AppListItem(
    appInfo: ApplicationInfo,
    packageManager: PackageManager,
    isSelected: Boolean,
    usageInfo: AppUsageInfo?,
    onClick: () -> Unit
) {
    val appName = remember { packageManager.getApplicationLabel(appInfo).toString() }
    val appIcon: Drawable = remember { packageManager.getApplicationIcon(appInfo) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) GoNullGreen.copy(alpha = 0.05f) else GoNullBlack)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            Image(
                bitmap = appIcon.toBitmap(width = 96, height = 96).asImageBitmap(),
                contentDescription = appName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // App name and package
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) GoNullGreen else GoNullWhite
                )
                if (usageInfo != null && usageInfo.totalTimeInForeground > 0) {
                    Text(
                        text = formatTime(usageInfo.totalTimeInForeground),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullYellow,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = appInfo.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray
                    )
                }
            }

            // Selection indicator
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GoNullGreen,
                    uncheckedColor = GoNullGray,
                    checkmarkColor = GoNullBlack
                )
            )
        }

        // Usage Bar (Moment of Realization)
        if (usageInfo != null && usageInfo.totalTimeInForeground > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            val animatedProgress by animateFloatAsState(
                targetValue = usageInfo.usagePercentage,
                label = "usage_bar"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(GoNullSurface, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            if (usageInfo.usagePercentage > 0.7f) GoNullRed else GoNullYellow,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m this week"
        minutes > 0 -> "${minutes}m this week"
        else -> "Less than a minute"
    }
}
