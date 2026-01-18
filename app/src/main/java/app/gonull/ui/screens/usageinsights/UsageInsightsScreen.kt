package app.gonull.ui.screens.usageinsights

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import app.gonull.ui.theme.*
import app.gonull.util.PermissionHelper
import app.gonull.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageInsightsScreen(
    viewModel: UsageInsightsViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    Scaffold(
        containerColor = GoNullBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoNullSurface)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Your App Usage - Last 7 Days",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select apps to block and take control",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
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
                    }

                    !hasUsagePermission -> {
                        // Empty state when permission is not granted
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = GoNullYellow,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Usage Stats Permission Needed",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoNullWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Grant usage access to see which apps are taking your time and get personalized recommendations.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoNullGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { PermissionHelper.openUsageStatsSettings(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoNullGreen,
                                    contentColor = GoNullBlack
                                )
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }

                    apps.isEmpty() && !isLoading -> {
                        // Empty state when no apps found
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No apps found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = GoNullGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadApps(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoNullGreen,
                                        contentColor = GoNullBlack
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    else -> {
                        // App list
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = apps,
                                key = { it.applicationInfo.packageName }
                            ) { appInfo ->
                                val isSelected = selectedApps.contains(appInfo.applicationInfo.packageName)

                                UsageAppCard(
                                    appInfo = appInfo,
                                    packageManager = context.packageManager,
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.toggleAppSelection(appInfo.applicationInfo.packageName)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GoNullSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedApps.isEmpty()) {
                            "Skip or select apps to block"
                        } else {
                            "${selectedApps.size} app${if (selectedApps.size > 1) "s" else ""} selected"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedApps.isEmpty()) GoNullGray else GoNullWhite
                    )

                    Button(
                        onClick = {
                            viewModel.saveAndContinue(
                                context = context,
                                packageManager = context.packageManager,
                                onComplete = onComplete
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoNullGreen,
                            contentColor = GoNullBlack
                        )
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageAppCard(
    appInfo: AppInfo,
    packageManager: PackageManager,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val appName = remember { packageManager.getApplicationLabel(appInfo.applicationInfo).toString() }
    val appIcon: Drawable = remember { packageManager.getApplicationIcon(appInfo.applicationInfo) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoNullGreen.copy(alpha = 0.1f) else GoNullSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, GoNullGreen)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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

                // App name and usage time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) GoNullGreen else GoNullWhite,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = if (appInfo.totalTimeInForeground > 0) {
                            "${TimeFormatter.formatUsageTime(appInfo.totalTimeInForeground)} this week"
                        } else {
                            "No usage data"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (appInfo.totalTimeInForeground > 0) GoNullYellow else GoNullGray
                    )
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
            if (appInfo.totalTimeInForeground > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = appInfo.usagePercentage,
                    label = "usage_bar"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(GoNullBlack, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(
                                if (appInfo.usagePercentage > 0.7f) GoNullRed else GoNullYellow,
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}
