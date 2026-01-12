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

/**
 * Fallback blocking service using UsageStatsManager polling.
 * This is a Google Play compliant alternative to AccessibilityService.
 *
 * Tradeoffs:
 * - 2-second detection lag (user sees blocked app briefly)
 * - Higher battery usage (constant polling)
 * + Google Play compliant
 * + No special permissions risk
 */
class UsageStatsPollingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private lateinit var usageStatsManager: UsageStatsManager

    private val checkInterval = 2000L // Check every 2 seconds
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0

    // Cache for blocked apps to avoid database queries on every poll
    private var blockedAppsCache: Set<String> = emptySet()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 60_000L // 1 minute

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            // Initial cache load
            updateBlockedAppsCache()

            while (isActive) {
                checkForegroundApp()
                delay(checkInterval)
            }
        }

        return START_STICKY
    }

    private suspend fun checkForegroundApp() {
        val currentTime = System.currentTimeMillis()

        // Update cache periodically
        if (currentTime - lastCacheUpdate > cacheTimeout) {
            updateBlockedAppsCache()
        }

        // Query usage stats for the last 5 seconds
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 5000,
            currentTime
        )

        // Find the most recently used app
        val foregroundApp = stats?.maxByOrNull { it.lastTimeUsed }?.packageName

        if (foregroundApp != null && foregroundApp != applicationContext.packageName) {
            // Check if app is blocked
            if (blockedAppsCache.contains(foregroundApp)) {
                // Check if there's an active unlock
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
            .kotlinx.coroutines.flow.first()
            .map { it.packageName }
            .toSet()
        lastCacheUpdate = System.currentTimeMillis()
    }

    private suspend fun blockApp(packageName: String) {
        // Debounce to prevent multiple triggers
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && now - lastBlockTime < 3000) return

        lastBlockedPackage = packageName
        lastBlockTime = now

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

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GoNull Active")
            .setContentText("Monitoring apps via Usage Stats")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Blocking Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when GoNull is actively blocking apps"
        }

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
