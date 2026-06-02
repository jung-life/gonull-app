package app.gonull.ui.screens.appselection

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

    fun loadApps(context: Context) {
        val hasPermission = PermissionHelper.hasUsageStatsPermission(context)
        
        // If we already loaded data and permission hasn't changed, skip to avoid flickering
        // BUT if we didn't have permission before and now we do, we MUST reload.
        if (_isLoaded.value && _hasUsagePermission.value == hasPermission) {
            return
        }

        _hasUsagePermission.value = hasPermission

        viewModelScope.launch {
            val packageManager = context.packageManager

            // Keyboards must never be blockable, and GoNull must not list itself.
            val imePackages = try {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.inputMethodList.mapNotNull { it.packageName }.toSet()
            } catch (e: Exception) {
                emptySet()
            }
            val ownPackage = context.packageName

            val allApps = withContext(Dispatchers.IO) {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { app ->
                        if (app.packageName == ownPackage) return@filter false
                        if (imePackages.contains(app.packageName)) return@filter false
                        val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                        val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                        isUserApp || hasLauncher
                    }
            }

            var mostUsedTime = 1L
            val usageMap = mutableMapOf<String, AppUsageInfo>()

            if (hasPermission) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                
                val stats = usageStatsManager.queryAndAggregateUsageStats(
                    calendar.timeInMillis,
                    System.currentTimeMillis()
                )

                if (stats.isNotEmpty()) {
                    stats.forEach { (pkg, stat) ->
                        val time = stat.totalTimeInForeground
                        if (time > mostUsedTime) mostUsedTime = time
                    }

                    stats.forEach { (pkg, stat) ->
                        val time = stat.totalTimeInForeground
                        usageMap[pkg] = AppUsageInfo(
                            packageName = pkg,
                            totalTimeInForeground = time,
                            usagePercentage = time.toFloat() / mostUsedTime
                        )
                    }
                }
            }
            
            _usageInfoMap.value = usageMap

            val sortedByUsage = allApps.sortedByDescending { usageMap[it.packageName]?.totalTimeInForeground ?: 0L }
            
            val mostUsed = if (hasPermission) sortedByUsage.take(10).filter { (usageMap[it.packageName]?.totalTimeInForeground ?: 0L) > 0 } else emptyList()
            
            val socialApps = mutableListOf<ApplicationInfo>()
            val entertainmentApps = mutableListOf<ApplicationInfo>()
            val productivityApps = mutableListOf<ApplicationInfo>()
            val otherApps = mutableListOf<ApplicationInfo>()

            val socialKeywords = listOf("facebook", "instagram", "twitter", "x.android", "tiktok", "snapchat", "linkedin", "reddit", "whatsapp", "telegram", "messenger")
            val entertainmentKeywords = listOf("youtube", "netflix", "disney", "spotify", "twitch", "prime.video", "hulu")
            val productivityKeywords = listOf("email", "calendar", "slack", "zoom", "teams", "notion", "todoist", "keep")

            allApps.forEach { app ->
                val pkg = app.packageName.lowercase()
                when {
                    socialKeywords.any { pkg.contains(it) } -> socialApps.add(app)
                    entertainmentKeywords.any { pkg.contains(it) } -> entertainmentApps.add(app)
                    productivityKeywords.any { pkg.contains(it) } -> productivityApps.add(app)
                    else -> otherApps.add(app)
                }
            }

            val categories = mutableListOf<AppCategory>()
            if (mostUsed.isNotEmpty()) categories.add(AppCategory("Top Used This Week (Realization Time)", mostUsed))
            if (socialApps.isNotEmpty()) categories.add(AppCategory("Social & Communication", socialApps.sortedBy { packageManager.getApplicationLabel(it).toString() }))
            if (entertainmentApps.isNotEmpty()) categories.add(AppCategory("Entertainment", entertainmentApps.sortedBy { packageManager.getApplicationLabel(it).toString() }))
            if (productivityApps.isNotEmpty()) categories.add(AppCategory("Productivity", productivityApps.sortedBy { packageManager.getApplicationLabel(it).toString() }))
            if (otherApps.isNotEmpty()) categories.add(AppCategory("All Other Apps", otherApps.sortedBy { packageManager.getApplicationLabel(it).toString() }))

            _categorizedApps.value = categories
            
            // Only pre-populate on first successful load
            if (!_isLoaded.value && _selectedApps.value.isEmpty()) {
                val recommended = (socialApps + entertainmentApps).map { it.packageName }.toSet()
                _selectedApps.value = recommended
            }
            
            _isLoaded.value = true
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
        viewModelScope.launch {
            _isSaving.value = true
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
            _isSaving.value = false
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
