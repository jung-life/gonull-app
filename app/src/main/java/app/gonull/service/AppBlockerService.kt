package app.gonull.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UsageLogEntity
import app.gonull.ui.screens.blocking.BlockingOverlayActivity
import app.gonull.util.Constants
import kotlinx.coroutines.*

class AppBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    // Cache for blocked apps to reduce database queries
    private var blockedAppsCache: Set<String> = emptySet()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 60_000L // 1 minute

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)

        // Initialize cache
        serviceScope.launch {
            updateBlockedAppsCache()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app
        if (packageName == applicationContext.packageName) return

        // Ignore system UI and launchers (optimization)
        if (packageName.startsWith("com.android.systemui")) return
        if (packageName.startsWith("com.android.launcher")) return
        if (packageName.startsWith("com.google.android.launcher")) return

        // Early exit if package is not in cache (optimization)
        if (!blockedAppsCache.contains(packageName)) return

        // Debounce to prevent multiple triggers
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockTime < 1000) return

        serviceScope.launch {
            checkAndBlockApp(packageName)
        }
    }

    private suspend fun updateBlockedAppsCache() {
        blockedAppsCache = database.blockedAppDao()
            .getActiveBlockedApps()
            .kotlinx.coroutines.flow.first()
            .map { it.packageName }
            .toSet()
        lastCacheUpdate = System.currentTimeMillis()
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        // Update cache if expired
        val now = System.currentTimeMillis()
        if (now - lastCacheUpdate > cacheTimeout) {
            updateBlockedAppsCache()
        }

        // Check if app is in blocked list (using cache)
        if (!blockedAppsCache.contains(packageName)) return

        // Check if there's an active unlock
        val activeUnlock = database.unlockRequestDao().getActiveUnlock(
            packageName,
            System.currentTimeMillis()
        )
        if (activeUnlock != null) return

        // Block the app
        lastBlockedPackage = packageName
        lastBlockTime = System.currentTimeMillis()

        // Log the block
        database.usageLogDao().insertLog(
            UsageLogEntity(
                packageName = packageName,
                eventType = Constants.EventType.BLOCKED
            )
        )

        // Launch blocking overlay
        withContext(Dispatchers.Main) {
            val intent = Intent(applicationContext, BlockingOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("PACKAGE_NAME", packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        var isRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isRunning = false
        return super.onUnbind(intent)
    }
}
