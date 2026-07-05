package app.gonull.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UsageLogEntity
import app.gonull.ui.screens.blocking.BlockingOverlayActivity
import app.gonull.util.Constants
import app.gonull.util.PermissionHelper
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

    // Cache for focus mode allowed apps (Gym/Meditation whitelist)
    private var focusModeAllowedCache: Set<String> = emptySet()
    private var isFocusModeActive: Boolean = false

    // Analog mode cache
    private var isAnalogModeActive: Boolean = false
    private var analogWhitelistCache: Set<String> = emptySet()

    // Input-method (keyboard) packages must NEVER be blocked, or the device
    // becomes unusable. Captured once at startup across all installed IMEs.
    private var protectedImePackages: Set<String> = emptySet()

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
        protectedImePackages = loadProtectedImePackages()

        // Register broadcast receiver for cache invalidation
        val cacheFilter = IntentFilter(ACTION_INVALIDATE_CACHE)
        val focusFilter = IntentFilter(FocusModeManager.ACTION_FOCUS_MODE_CHANGED)
        ContextCompat.registerReceiver(
            this, cacheInvalidationReceiver, cacheFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        ContextCompat.registerReceiver(
            this, focusModeReceiver, focusFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Initialize caches
        serviceScope.launch {
            updateBlockedAppsCache()
            updateFocusModeCache()
        }
    }

    private suspend fun updateFocusModeCache() {
        val activeModes = focusModeManager.getActiveModes()
        val nonAnalogModes = activeModes.filter { !it.isAnalogMode() }
        isFocusModeActive = nonAnalogModes.isNotEmpty()
        focusModeAllowedCache = nonAnalogModes
            .flatMap { it.getAllowedPackagesList() }
            .toSet()

        // Update analog mode cache
        val analogMode = activeModes.find { it.isAnalogMode() }
        isAnalogModeActive = analogMode != null
        analogWhitelistCache = if (isAnalogModeActive) {
            analogMode!!.getAllowedPackagesList().toSet()
        } else {
            emptySet()
        }

        Log.d(TAG, "Focus mode cache updated. Analog active: $isAnalogModeActive, Focus active: $isFocusModeActive, ${focusModeAllowedCache.size} focus-allowed apps")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app, system/launcher packages, and keyboards. Blocking an
        // input method would leave the user unable to type anywhere on the device.
        if (packageName == applicationContext.packageName) return
        if (PermissionHelper.isSystemOrCriticalService(packageName)) return
        if (protectedImePackages.contains(packageName)) return

        // ANALOG MODE: block everything except whitelist
        if (isAnalogModeActive) {
            if (analogWhitelistCache.contains(packageName)) return
            // Not in whitelist — block it
            val now = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && now - lastBlockTime < 1000) return
            serviceScope.launch {
                blockApp(packageName)
            }
            return
        }

        // FOCUS MODE (Gym/Meditation): block everything except allowed apps
        if (isFocusModeActive) {
            if (focusModeAllowedCache.contains(packageName)) return
            // Not in allowed list — block it
            val now = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && now - lastBlockTime < 1000) return
            serviceScope.launch {
                blockApp(packageName)
            }
            return
        }

        // Normal mode: early exit if package is not in blocked cache
        if (!blockedAppsCache.contains(packageName)) return

        // Debounce to prevent multiple triggers
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockTime < 1000) return

        serviceScope.launch {
            checkAndBlockApp(packageName)
        }
    }

    private suspend fun updateBlockedAppsCache() {
        val active = database.blockedAppDao().getActiveBlockedApps().first()
        // Defensively drop any never-blockable packages (keyboards, our own app) that
        // may have been saved before they were filtered out of app selection.
        val (neverBlockable, blockable) = active.partition { app ->
            app.packageName == applicationContext.packageName ||
                protectedImePackages.contains(app.packageName)
        }
        neverBlockable.forEach { database.blockedAppDao().deleteBlockedAppByPackage(it.packageName) }
        blockedAppsCache = blockable.map { it.packageName }.toSet()
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

        // Check if app is allowed by an active focus mode (Gym/Meditation mode)
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

        blockApp(packageName)
    }

    private suspend fun blockApp(packageName: String) {
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

    /** All installed input-method (keyboard) packages — never blockable. */
    private fun loadProtectedImePackages(): Set<String> {
        return try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.inputMethodList.mapNotNull { it.packageName }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load input-method packages", e)
            emptySet()
        }
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
