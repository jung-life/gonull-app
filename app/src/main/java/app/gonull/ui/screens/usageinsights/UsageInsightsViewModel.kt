package app.gonull.ui.screens.usageinsights

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.util.PermissionHelper
import app.gonull.util.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

data class AppInfo(
    val applicationInfo: ApplicationInfo,
    val totalTimeInForeground: Long,
    val usagePercentage: Float // Relative to the most used app
)

class UsageInsightsViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    /**
     * Load all installed apps with usage statistics for the last 7 days.
     * Apps are sorted by usage time in descending order (most used first).
     */
    fun loadApps(context: Context) {
        Log.d("UsageInsights", "loadApps called")
        val hasPermission = PermissionHelper.hasUsageStatsPermission(context)
        _hasUsagePermission.value = hasPermission
        Log.d("UsageInsights", "Has usage permission: $hasPermission")

        viewModelScope.launch {
            _isLoading.value = true

            val packageManager = context.packageManager

            // Get all user-installed apps and system apps with launchers
            val allApps = withContext(Dispatchers.IO) {
                val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { app ->
                        val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                        val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                        isUserApp || hasLauncher
                    }
                Log.d("UsageInsights", "Found ${apps.size} apps")
                apps
            }

            // Build usage statistics map
            var mostUsedTime = 1L
            val usageMap = mutableMapOf<String, Pair<Long, Float>>()

            if (hasPermission) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7) // Last 7 days

                val stats = usageStatsManager.queryAndAggregateUsageStats(
                    calendar.timeInMillis,
                    System.currentTimeMillis()
                )

                // Find the most used app time
                stats.forEach { (_, stat) ->
                    val time = stat.totalTimeInForeground
                    if (time > mostUsedTime) mostUsedTime = time
                }

                // Calculate usage percentages
                stats.forEach { (pkg, stat) ->
                    val time = stat.totalTimeInForeground
                    val percentage = time.toFloat() / mostUsedTime
                    usageMap[pkg] = Pair(time, percentage)
                }
            }

            // Create AppInfo objects and sort by usage time (descending)
            val appInfoList = allApps.map { app ->
                val (time, percentage) = usageMap[app.packageName] ?: Pair(0L, 0f)

                AppInfo(
                    applicationInfo = app,
                    totalTimeInForeground = time,
                    usagePercentage = percentage
                )
            }.sortedByDescending { it.totalTimeInForeground }

            Log.d("UsageInsights", "Setting ${appInfoList.size} apps to state")
            _apps.value = appInfoList
            _isLoading.value = false
            Log.d("UsageInsights", "Loading complete. isLoading=false, apps size=${_apps.value.size}")
        }
    }

    /**
     * Toggle selection of an app for blocking.
     */
    fun toggleAppSelection(packageName: String) {
        _selectedApps.value = if (_selectedApps.value.contains(packageName)) {
            _selectedApps.value - packageName
        } else {
            _selectedApps.value + packageName
        }
    }

    /**
     * Save selected apps to the blocklist database,
     * mark initial setup as complete, and trigger navigation.
     */
    fun saveAndContinue(context: Context, packageManager: PackageManager, onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Save each selected app to the blocklist
                _selectedApps.value.forEach { packageName ->
                    val appInfo = _apps.value.find { it.applicationInfo.packageName == packageName }
                    if (appInfo != null) {
                        val appName = appInfo.applicationInfo.loadLabel(packageManager).toString()

                        database.blockedAppDao().insertBlockedApp(
                            BlockedAppEntity(
                                packageName = packageName,
                                appName = appName
                            )
                        )
                    }
                }

                // Mark initial setup as complete
                PreferenceHelper.setInitialSetupComplete(context)
            }

            // Trigger navigation to Home screen
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
