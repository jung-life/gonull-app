package app.gonull.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import app.gonull.util.Constants
import app.gonull.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Cached app information with usage data
 */
data class CachedAppInfo(
    val applicationInfo: ApplicationInfo,
    val appName: String,
    val packageName: String,
    val totalTimeInForeground: Long,
    val usagePercentage: Float,
    val isUsualSuspect: Boolean
)

/**
 * Singleton cache for preloaded app data.
 * This allows the app to start loading data as soon as permissions are granted,
 * so the UsageInsightsScreen and AppSelectionScreen can display data immediately.
 */
object AppDataCache {
    private const val TAG = "AppDataCache"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _apps = MutableStateFlow<List<CachedAppInfo>>(emptyList())
    val apps: StateFlow<List<CachedAppInfo>> = _apps.asStateFlow()

    private val _usualSuspects = MutableStateFlow<List<CachedAppInfo>>(emptyList())
    val usualSuspects: StateFlow<List<CachedAppInfo>> = _usualSuspects.asStateFlow()

    private val _otherApps = MutableStateFlow<List<CachedAppInfo>>(emptyList())
    val otherApps: StateFlow<List<CachedAppInfo>> = _otherApps.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    /**
     * Preload app data. Call this when all permissions are granted.
     * Safe to call multiple times - will only load once unless force=true.
     */
    suspend fun preload(context: Context, force: Boolean = false) {
        if (_isLoading.value) {
            Log.d(TAG, "Already loading, skipping")
            return
        }

        if (_isLoaded.value && !force) {
            Log.d(TAG, "Already loaded, skipping (use force=true to reload)")
            return
        }

        Log.d(TAG, "Starting preload...")
        _isLoading.value = true

        try {
            val hasPermission = PermissionHelper.hasUsageStatsPermission(context)
            _hasUsagePermission.value = hasPermission
            Log.d(TAG, "Has usage permission: $hasPermission")

            val packageManager = context.packageManager

            // Get all installed apps with launchers
            val allApps = withContext(Dispatchers.IO) {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { app ->
                        val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                        val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                        // Include user apps and system apps that have launchers
                        isUserApp || hasLauncher
                    }
            }
            Log.d(TAG, "Found ${allApps.size} apps")

            // Build usage statistics map
            var mostUsedTime = 1L
            val usageMap = mutableMapOf<String, Long>()

            if (hasPermission) {
                withContext(Dispatchers.IO) {
                    try {
                        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -7) // Last 7 days

                        val stats = usageStatsManager.queryAndAggregateUsageStats(
                            calendar.timeInMillis,
                            System.currentTimeMillis()
                        )

                        stats.forEach { (pkg, stat) ->
                            val time = stat.totalTimeInForeground
                            usageMap[pkg] = time
                            if (time > mostUsedTime) mostUsedTime = time
                        }
                        Log.d(TAG, "Loaded usage stats for ${stats.size} apps")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading usage stats", e)
                    }
                }
            }

            // Create CachedAppInfo objects
            val cachedApps = allApps.map { app ->
                val pkg = app.packageName.lowercase()
                val time = usageMap[app.packageName] ?: 0L
                val percentage = if (mostUsedTime > 0) time.toFloat() / mostUsedTime else 0f

                // Check if this is a usual suspect
                val isUsualSuspect = Constants.USUAL_SUSPECTS.keys.any { suspect ->
                    pkg == suspect.lowercase()
                } || Constants.USUAL_SUSPECT_KEYWORDS.any { keyword ->
                    pkg.contains(keyword)
                }

                CachedAppInfo(
                    applicationInfo = app,
                    appName = app.loadLabel(packageManager).toString(),
                    packageName = app.packageName,
                    totalTimeInForeground = time,
                    usagePercentage = percentage,
                    isUsualSuspect = isUsualSuspect
                )
            }

            // Sort: Usual suspects first (by usage), then others (by usage)
            val suspects = cachedApps
                .filter { it.isUsualSuspect }
                .sortedByDescending { it.totalTimeInForeground }

            val others = cachedApps
                .filter { !it.isUsualSuspect }
                .sortedByDescending { it.totalTimeInForeground }

            _usualSuspects.value = suspects
            _otherApps.value = others
            _apps.value = suspects + others

            Log.d(TAG, "Preload complete: ${suspects.size} usual suspects, ${others.size} other apps")
            _isLoaded.value = true

        } catch (e: Exception) {
            Log.e(TAG, "Error during preload", e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Force reload the cache
     */
    suspend fun reload(context: Context) {
        preload(context, force = true)
    }

    /**
     * Clear the cache
     */
    fun clear() {
        _apps.value = emptyList()
        _usualSuspects.value = emptyList()
        _otherApps.value = emptyList()
        _isLoaded.value = false
        _hasUsagePermission.value = false
    }
}
