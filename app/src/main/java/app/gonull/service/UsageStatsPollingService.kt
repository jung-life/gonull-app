package app.gonull.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.gonull.R
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UsageLogEntity
import app.gonull.ui.MainActivity
import app.gonull.ui.screens.blocking.BlockingOverlayActivity
import app.gonull.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UsageStatsPollingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var focusModeManager: FocusModeManager

    private val checkInterval = 2000L
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    private var blockedAppsCache: Set<String> = emptySet()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 60_000L

    // Focus mode cache (Gym/Meditation)
    private var isFocusModeActive: Boolean = false
    private var focusModeAllowedCache: Set<String> = emptySet()

    // Analog mode cache
    private var isAnalogModeActive: Boolean = false
    private var analogWhitelistCache: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        focusModeManager = FocusModeManager.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Use a standard public icon to avoid ResourceNotFoundException
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            updateBlockedAppsCache()
            updateFocusModeCache()
            while (isActive) {
                checkForegroundApp()
                delay(checkInterval)
            }
        }

        return START_STICKY
    }

    private suspend fun updateFocusModeCache() {
        val activeModes = focusModeManager.getActiveModes()
        val nonAnalogModes = activeModes.filter { !it.isAnalogMode() }
        isFocusModeActive = nonAnalogModes.isNotEmpty()
        focusModeAllowedCache = nonAnalogModes
            .flatMap { it.getAllowedPackagesList() }
            .toSet()

        val analogMode = activeModes.find { it.isAnalogMode() }
        isAnalogModeActive = analogMode != null
        analogWhitelistCache = if (isAnalogModeActive) {
            analogMode!!.getAllowedPackagesList().toSet()
        } else {
            emptySet()
        }
    }

    private suspend fun checkForegroundApp() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCacheUpdate > cacheTimeout) {
            updateBlockedAppsCache()
            updateFocusModeCache()
        }

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 5000,
            currentTime
        )

        val foregroundApp = stats?.maxByOrNull { it.lastTimeUsed }?.packageName

        if (foregroundApp != null && foregroundApp != applicationContext.packageName
            && !isSystemOrLauncher(foregroundApp)) {

            // Analog mode: block everything except whitelist
            if (isAnalogModeActive) {
                if (!analogWhitelistCache.contains(foregroundApp)) {
                    blockApp(foregroundApp)
                }
                return
            }

            // Focus mode (Gym/Meditation): block everything except allowed apps
            if (isFocusModeActive) {
                if (!focusModeAllowedCache.contains(foregroundApp)) {
                    blockApp(foregroundApp)
                }
                return
            }

            // Normal mode
            if (blockedAppsCache.contains(foregroundApp)) {
                val activeUnlock = database.unlockRequestDao().getActiveUnlock(
                    foregroundApp,
                    currentTime
                )
                if (activeUnlock == null) {
                    blockApp(foregroundApp)
                }
            }
        }
    }

    private suspend fun updateBlockedAppsCache() {
        blockedAppsCache = database.blockedAppDao()
            .getActiveBlockedApps()
            .first()
            .map { it.packageName }
            .toSet()
        lastCacheUpdate = System.currentTimeMillis()
    }

    private suspend fun blockApp(packageName: String) {
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockTime < 3000) return

        lastBlockedPackage = packageName
        lastBlockTime = now

        database.usageLogDao().insertLog(
            UsageLogEntity(
                packageName = packageName,
                eventType = Constants.EventType.BLOCKED
            )
        )

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

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GoNull Active")
            .setContentText("Monitoring apps")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Blocking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun isSystemOrLauncher(packageName: String): Boolean {
        return packageName.startsWith("com.android.systemui")
                || packageName.startsWith("com.android.launcher")
                || packageName.startsWith("com.google.android.launcher")
                || packageName.startsWith("com.google.android.apps.nexuslauncher")
                || packageName.startsWith("com.sec.android.app.launcher")
                || packageName.startsWith("com.sec.android.launcher")
                || packageName.startsWith("com.miui.home")
                || packageName.startsWith("com.miui.launcher")
                || packageName.startsWith("com.oneplus.launcher")
                || packageName.startsWith("com.oppo.launcher")
                || packageName.startsWith("com.realme.launcher")
                || packageName.startsWith("com.vivo.launcher")
                || packageName.startsWith("com.huawei.android.launcher")
                || packageName.startsWith("com.honor.launcher")
                || packageName.startsWith("com.motorola.launcher")
                || packageName.startsWith("com.nothing.launcher")
                || packageName.startsWith("com.teslacoilsw.launcher")
                || packageName.startsWith("com.microsoft.launcher")
                || packageName.startsWith("com.actionlauncher")
                || packageName.startsWith("com.novalauncher")
                || packageName.endsWith(".launcher")
                || packageName.endsWith(".home")
                || packageName.startsWith("com.android.settings")
    }

    companion object {
        private const val CHANNEL_ID = "usage_stats_blocking"
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, UsageStatsPollingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, UsageStatsPollingService::class.java)
            context.stopService(intent)
        }
    }
}
