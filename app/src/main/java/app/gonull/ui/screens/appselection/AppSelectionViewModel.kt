package app.gonull.ui.screens.appselection

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppSelectionViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val installedApps: StateFlow<List<ApplicationInfo>> = _installedApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadInstalledApps(packageManager: PackageManager) {
        viewModelScope.launch {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { app ->
                    // Filter out system apps that don't have a launcher icon
                    val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                    isUserApp || hasLauncher
                }
                .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }

            _installedApps.value = apps
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterApps()
    }

    private fun filterApps() {
        val query = _searchQuery.value.lowercase()
        if (query.isEmpty()) {
            // Show all apps when search is empty
            return
        }

        viewModelScope.launch {
            // Note: For simplicity in Phase 1, keeping all apps visible
            // Can implement filtering in Phase 2
        }
    }

    fun toggleAppSelection(packageName: String) {
        _selectedApps.value = if (_selectedApps.value.contains(packageName)) {
            _selectedApps.value - packageName
        } else {
            _selectedApps.value + packageName
        }
    }

    fun saveSelectedApps(packageManager: PackageManager) {
        viewModelScope.launch {
            _selectedApps.value.forEach { packageName ->
                val appInfo = _installedApps.value.find { it.packageName == packageName }
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
    }
}
