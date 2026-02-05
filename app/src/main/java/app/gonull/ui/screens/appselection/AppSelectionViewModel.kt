package app.gonull.ui.screens.appselection

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
import app.gonull.util.Constants
import app.gonull.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppCategory(
    val name: String,
    val apps: List<ApplicationInfo>
)

data class AppUsageInfo(
    val packageName: String,
    val totalTimeInForeground: Long,
    val usagePercentage: Float // Relative to the most used app
)

class AppSelectionViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _categorizedApps = MutableStateFlow<List<AppCategory>>(emptyList())
    val categorizedApps: StateFlow<List<AppCategory>> = _categorizedApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _usageInfoMap = MutableStateFlow<Map<String, AppUsageInfo>>(emptyMap())
    val usageInfoMap: StateFlow<Map<String, AppUsageInfo>> = _usageInfoMap.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Track existing blocked apps to avoid re-selecting defaults
    private var existingBlockedApps: Set<String> = emptySet()

    private var isLoadingInProgress = false

    fun loadApps(context: Context) {
        // Prevent multiple simultaneous loads
        if (isLoadingInProgress) {
            Log.d("AppSelection", "Load already in progress, skipping")
            return
        }

        Log.d("AppSelection", "loadApps called")
        isLoadingInProgress = true

        // Check permission directly - this is the authoritative source
        _hasUsagePermission.value = PermissionHelper.hasUsageStatsPermission(context)
        Log.d("AppSelection", "Has usage permission: ${_hasUsagePermission.value}")

        viewModelScope.launch {
            // Load existing blocked apps from database
            val blockedApps = withContext(Dispatchers.IO) {
                database.blockedAppDao().getActiveBlockedAppsSync()
            }
            existingBlockedApps = blockedApps.map { it.packageName }.toSet()

            // Pre-select already blocked apps
            _selectedApps.value = existingBlockedApps

            // Check if cache is ready, if not trigger preload
            if (!AppDataCache.isLoaded.value) {
                Log.d("AppSelection", "Cache not ready, triggering preload...")
                AppDataCache.preload(context)
            }

            // Now load from cache
            Log.d("AppSelection", "Loading from cache, apps count: ${AppDataCache.apps.value.size}")

            // If cache is empty, try loading directly
            if (AppDataCache.apps.value.isEmpty()) {
                Log.w("AppSelection", "Cache is empty, loading apps directly...")
                loadAppsDirect(context)
            } else {
                loadFromCache(context)
            }

            _isLoaded.value = true
            isLoadingInProgress = false
            Log.d("AppSelection", "App selection loaded successfully with ${_categorizedApps.value.sumOf { it.apps.size }} apps")
        }
    }

    private suspend fun loadAppsDirect(context: Context) {
        val packageManager = context.packageManager

        withContext(Dispatchers.IO) {
            try {
                val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                Log.d("AppSelection", "Direct load: found ${installedApps.size} total installed apps")

                val launchableApps = installedApps.filter { app ->
                    // Skip our own app
                    if (app.packageName == context.packageName) return@filter false

                    val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val hasLauncher = try {
                        packageManager.getLaunchIntentForPackage(app.packageName) != null
                    } catch (e: Exception) {
                        false
                    }

                    isUserApp || isUpdatedSystemApp || hasLauncher
                }

                Log.d("AppSelection", "Direct load: filtered to ${launchableApps.size} launchable apps")

                // Separate usual suspects from other apps
                val usualSuspects = mutableListOf<ApplicationInfo>()
                val otherApps = mutableListOf<ApplicationInfo>()

                launchableApps.forEach { app ->
                    val pkg = app.packageName.lowercase()
                    val isUsualSuspect = Constants.USUAL_SUSPECTS.keys.any { suspect ->
                        pkg == suspect.lowercase()
                    } || Constants.USUAL_SUSPECT_KEYWORDS.any { keyword ->
                        pkg.contains(keyword)
                    }

                    if (isUsualSuspect) {
                        usualSuspects.add(app)
                    } else {
                        otherApps.add(app)
                    }
                }

                // Sort alphabetically
                usualSuspects.sortBy { packageManager.getApplicationLabel(it).toString() }
                otherApps.sortBy { packageManager.getApplicationLabel(it).toString() }

                Log.d("AppSelection", "Direct load: ${usualSuspects.size} usual suspects, ${otherApps.size} other apps")

                // Build categories
                val categories = mutableListOf<AppCategory>()
                if (usualSuspects.isNotEmpty()) {
                    categories.add(AppCategory("The Usual Suspects", usualSuspects))
                }
                if (otherApps.isNotEmpty()) {
                    categories.add(AppCategory("All Apps", otherApps))
                }

                withContext(Dispatchers.Main) {
                    _categorizedApps.value = categories

                    // Auto-select usual suspects if first load and no existing blocked apps
                    if (!_isLoaded.value && existingBlockedApps.isEmpty()) {
                        _selectedApps.value = usualSuspects.map { it.packageName }.toSet()
                    }
                }
            } catch (e: Exception) {
                Log.e("AppSelection", "Error in direct load", e)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun loadFromCache(context: Context) {
        val cachedApps = AppDataCache.apps.value
        val usualSuspectsCached = AppDataCache.usualSuspects.value
        val otherAppsCached = AppDataCache.otherApps.value

        Log.d("AppSelection", "loadFromCache: cachedApps=${cachedApps.size}, suspects=${usualSuspectsCached.size}, others=${otherAppsCached.size}")

        // Build usage info map from cache
        val usageMap = cachedApps.associate { cached ->
            cached.packageName to AppUsageInfo(
                packageName = cached.packageName,
                totalTimeInForeground = cached.totalTimeInForeground,
                usagePercentage = cached.usagePercentage
            )
        }
        _usageInfoMap.value = usageMap

        // Get ApplicationInfo lists
        val usualSuspects = usualSuspectsCached.map { it.applicationInfo }
        val usualSuspectsPackages = usualSuspects.map { it.packageName }.toSet()

        // Get top used (non-usual suspects with usage)
        val mostUsed = if (_hasUsagePermission.value) {
            otherAppsCached
                .filter { it.totalTimeInForeground > 0 }
                .take(10)
                .map { it.applicationInfo }
        } else emptyList()

        // Categorize remaining apps
        val socialApps = mutableListOf<ApplicationInfo>()
        val entertainmentApps = mutableListOf<ApplicationInfo>()
        val productivityApps = mutableListOf<ApplicationInfo>()
        val otherApps = mutableListOf<ApplicationInfo>()

        val socialKeywords = listOf("facebook", "instagram", "twitter", "x.android", "tiktok", "snapchat", "linkedin", "reddit", "whatsapp", "telegram", "messenger")
        val entertainmentKeywords = listOf("youtube", "netflix", "disney", "spotify", "twitch", "prime.video", "hulu")
        val productivityKeywords = listOf("email", "calendar", "slack", "zoom", "teams", "notion", "todoist", "keep")

        otherAppsCached.forEach { cached ->
            val pkg = cached.packageName.lowercase()
            val app = cached.applicationInfo
            when {
                socialKeywords.any { pkg.contains(it) } -> socialApps.add(app)
                entertainmentKeywords.any { pkg.contains(it) } -> entertainmentApps.add(app)
                productivityKeywords.any { pkg.contains(it) } -> productivityApps.add(app)
                else -> otherApps.add(app)
            }
        }

        // Helper to sort by usage (descending)
        fun List<ApplicationInfo>.sortedByUsage(): List<ApplicationInfo> {
            return this.sortedByDescending { usageMap[it.packageName]?.totalTimeInForeground ?: 0L }
        }

        // Build categories - sorted by usage
        val categories = mutableListOf<AppCategory>()
        if (usualSuspects.isNotEmpty()) categories.add(AppCategory("The Usual Suspects", usualSuspects))
        if (mostUsed.isNotEmpty()) categories.add(AppCategory("Your Top Used This Week", mostUsed))
        if (socialApps.isNotEmpty()) categories.add(AppCategory("Social & Communication", socialApps.sortedByUsage()))
        if (entertainmentApps.isNotEmpty()) categories.add(AppCategory("Entertainment", entertainmentApps.sortedByUsage()))
        if (productivityApps.isNotEmpty()) categories.add(AppCategory("Productivity", productivityApps.sortedByUsage()))
        if (otherApps.isNotEmpty()) categories.add(AppCategory("All Other Apps", otherApps.sortedByUsage()))

        _categorizedApps.value = categories
        Log.d("AppSelection", "Loaded ${categories.size} categories from cache")

        // Only auto-select usual suspects if first load AND user has no existing blocked apps
        if (!_isLoaded.value && existingBlockedApps.isEmpty()) {
            _selectedApps.value = usualSuspectsPackages
            Log.d("AppSelection", "Auto-selected ${usualSuspectsPackages.size} usual suspects")
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppSelection(packageName: String) {
        _selectedApps.value = if (_selectedApps.value.contains(packageName)) {
            _selectedApps.value - packageName
        } else {
            _selectedApps.value + packageName
        }
    }

    fun saveSelectedApps(context: Context, packageManager: PackageManager, onComplete: () -> Unit) {
        if (_isSaving.value) return // Prevent double-save

        _isSaving.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // First, remove apps that were unselected (in existingBlockedApps but not in selectedApps)
                    val appsToRemove = existingBlockedApps - _selectedApps.value
                    appsToRemove.forEach { packageName ->
                        database.blockedAppDao().deleteBlockedAppByPackage(packageName)
                    }

                    // Then, add newly selected apps
                    _selectedApps.value.forEach { packageName ->
                        val categories = _categorizedApps.value
                        val appInfo = categories.flatMap { it.apps }.find { it.packageName == packageName }
                        if (appInfo != null) {
                            val appName = appInfo.loadLabel(packageManager).toString()

                            database.blockedAppDao().insertBlockedApp(
                                BlockedAppEntity(
                                    packageName = packageName,
                                    appName = appName
                                )
                            )
                        }
                    }
                }

                // Immediately invalidate the AppBlockerService cache so blocking takes effect right away
                AppBlockerService.invalidateCache(context)

                // Navigate back on main thread after save completes
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
