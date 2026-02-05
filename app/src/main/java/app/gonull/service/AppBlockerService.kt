package app.gonull.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UsageLogEntity
import app.gonull.ui.screens.blocking.BlockingOverlayActivity
import app.gonull.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppBlockerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private lateinit var focusModeManager: FocusModeManager

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    // Cache for blocked apps to reduce database queries
    private var blockedAppsCache: Set<String> = emptySet()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 60_000L // 1 minute

    // Cache for focus mode allowed apps
    private var focusModeAllowedCache: Set<String> = emptySet()

    // Broadcast receiver for cache invalidation
    private val cacheInvalidationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received cache invalidation broadcast")
            serviceScope.launch {
                updateBlockedAppsCache()
                updateFocusModeCache()
            }
        }
    }

    // Broadcast receiver for focus mode changes
    private val focusModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received focus mode change broadcast")
            serviceScope.launch {
                updateFocusModeCache()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        focusModeManager = FocusModeManager.getInstance(applicationContext)

        // Register broadcast receiver for cache invalidation
        val cacheFilter = IntentFilter(ACTION_INVALIDATE_CACHE)
        val focusFilter = IntentFilter(FocusModeManager.ACTION_FOCUS_MODE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cacheInvalidationReceiver, cacheFilter, RECEIVER_NOT_EXPORTED)
            registerReceiver(focusModeReceiver, focusFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(cacheInvalidationReceiver, cacheFilter)
            registerReceiver(focusModeReceiver, focusFilter)
        }

        // Initialize caches
        serviceScope.launch {
            updateBlockedAppsCache()
            updateFocusModeCache()
        }
    }

    private suspend fun updateFocusModeCache() {
        val activeModes = focusModeManager.getActiveModes()
        focusModeAllowedCache = activeModes
            .flatMap { it.getAllowedPackagesList() }
            .toSet()
        Log.d(TAG, "Focus mode cache updated with ${focusModeAllowedCache.size} allowed apps")
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
            .first()
            .map { it.packageName }
            .toSet()
        lastCacheUpdate = System.currentTimeMillis()
        Log.d(TAG, "Cache updated with ${blockedAppsCache.size} blocked apps: $blockedAppsCache")
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        // Update cache if expired
        val now = System.currentTimeMillis()
        if (now - lastCacheUpdate > cacheTimeout) {
            updateBlockedAppsCache()
            updateFocusModeCache()
        }

        // Check if app is in blocked list (using cache)
        if (!blockedAppsCache.contains(packageName)) return

        // Check if app is allowed by an active focus mode (Gym/Yoga mode)
        if (focusModeAllowedCache.contains(packageName)) {
            Log.d(TAG, "App $packageName is allowed by active focus mode")
            return
        }

        // Double-check with database for focus mode (in case cache is stale)
        if (focusModeManager.isPackageAllowedByFocusMode(packageName)) {
            Log.d(TAG, "App $packageName is allowed by focus mode (db check)")
            updateFocusModeCache()
            return
        }

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
        try {
            unregisterReceiver(cacheInvalidationReceiver)
            unregisterReceiver(focusModeReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers", e)
        }
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "AppBlockerService"
        const val ACTION_INVALIDATE_CACHE = "app.gonull.ACTION_INVALIDATE_BLOCKED_APPS_CACHE"

        var isRunning = false
            private set

        /**
         * Call this method after blocking/unblocking apps to immediately update the service cache.
         * This ensures newly blocked apps are blocked right away without waiting for cache timeout.
         */
        fun invalidateCache(context: Context) {
            Log.d(TAG, "Sending cache invalidation broadcast")
            val intent = Intent(ACTION_INVALIDATE_CACHE)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        }
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
