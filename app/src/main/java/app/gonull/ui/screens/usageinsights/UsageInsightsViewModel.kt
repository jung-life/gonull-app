package app.gonull.ui.screens.usageinsights

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.AppDataCache
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.service.AppBlockerService
import app.gonull.util.PermissionHelper
import app.gonull.util.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(
    val applicationInfo: ApplicationInfo,
    val totalTimeInForeground: Long,
    val usagePercentage: Float, // Relative to the most used app
    val isUsualSuspect: Boolean = false
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Update the search query for filtering apps.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Load all installed apps with usage statistics.
     * Uses cached data from AppDataCache if available, otherwise loads fresh.
     */
    fun loadApps(context: Context) {
        Log.d("UsageInsights", "========== loadApps called ==========")

        // Check permission directly - this is the authoritative source
        _hasUsagePermission.value = PermissionHelper.hasUsageStatsPermission(context)
        Log.d("UsageInsights", "Has usage permission: ${_hasUsagePermission.value}")

        viewModelScope.launch {
            _isLoading.value = true

            // Check if cache is already loaded
            if (AppDataCache.isLoaded.value) {
                Log.d("UsageInsights", "Using cached app data")
                loadFromCache()
            } else {
                Log.d("UsageInsights", "Cache not ready, triggering preload...")
                // Trigger preload and wait for it
                AppDataCache.preload(context)
                loadFromCache()
            }

            _isLoading.value = false
        }
    }

    private fun loadFromCache() {
        val cachedApps = AppDataCache.apps.value

        // Convert CachedAppInfo to AppInfo
        val appInfoList = cachedApps.map { cached ->
            AppInfo(
                applicationInfo = cached.applicationInfo,
                totalTimeInForeground = cached.totalTimeInForeground,
                usagePercentage = cached.usagePercentage,
                isUsualSuspect = cached.isUsualSuspect
            )
        }

        _apps.value = appInfoList
        Log.d("UsageInsights", "Loaded ${appInfoList.size} apps from cache")

        // Auto-select usual suspects by default (only if nothing selected yet)
        if (_selectedApps.value.isEmpty()) {
            val usualSuspectPackages = appInfoList
                .filter { it.isUsualSuspect }
                .map { it.applicationInfo.packageName }
                .toSet()
            _selectedApps.value = usualSuspectPackages
            Log.d("UsageInsights", "Auto-selected ${usualSuspectPackages.size} usual suspects")
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

            // Immediately invalidate the AppBlockerService cache so blocking takes effect right away
            AppBlockerService.invalidateCache(context)

            // Trigger navigation to Home screen
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
