package app.gonull.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
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
            updateAnalogModeCache()
            while (isActive) {
                checkForegroundApp()
                delay(checkInterval)
            }
        }

        return START_STICKY
    }

    private suspend fun updateAnalogModeCache() {
        isAnalogModeActive = focusModeManager.isAnalogModeActive()
        if (isAnalogModeActive) {
            analogWhitelistCache = focusModeManager.getAnalogWhitelist()
        } else {
            analogWhitelistCache = emptySet()
        }
    }

    private suspend fun checkForegroundApp() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCacheUpdate > cacheTimeout) {
            updateBlockedAppsCache()
            updateAnalogModeCache()
        }

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 5000,
            currentTime
        )

        val foregroundApp = stats?.maxByOrNull { it.lastTimeUsed }?.packageName

        if (foregroundApp != null && foregroundApp != applicationContext.packageName) {
            // Analog mode: block everything except whitelist
            if (isAnalogModeActive) {
                if (!analogWhitelistCache.contains(foregroundApp)
                    && !foregroundApp.startsWith("com.android.systemui")
                    && !foregroundApp.startsWith("com.android.launcher")
                    && !foregroundApp.startsWith("com.google.android.launcher")
                ) {
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
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Changed to public icon
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
