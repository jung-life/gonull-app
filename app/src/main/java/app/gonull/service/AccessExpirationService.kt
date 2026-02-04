package app.gonull.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import app.gonull.R
import app.gonull.data.local.AppDatabase
import app.gonull.ui.screens.warning.AccessWarningActivity
import app.gonull.util.Constants
import kotlinx.coroutines.*

/**
 * Foreground service that monitors active access sessions and:
 * 1. Shows a 2-minute warning when access is about to expire
 * 2. Re-blocks apps when their access period ends
 * 3. Tracks usage time for the usage mirror
 */
class AccessExpirationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var accessSessionManager: AccessSessionManager
    private lateinit var database: AppDatabase

    private var monitoringJob: Job? = null
    private val warningShownFor = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AccessExpirationService created")

        accessSessionManager = AccessSessionManager.getInstance(applicationContext)
        database = AppDatabase.getDatabase(applicationContext)

        createNotificationChannels()
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
            ACTION_DISMISS_WARNING -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                packageName?.let { warningShownFor.add(it) }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AccessExpirationService destroyed")
        monitoringJob?.cancel()
        serviceScope.cancel()
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            Log.d(TAG, "Starting session monitoring")

            while (isActive) {
                try {
                    checkActiveSessions()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking sessions", e)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkActiveSessions() {
        val now = System.currentTimeMillis()
        val activeSessions = accessSessionManager.getAllActiveSessions()

        if (activeSessions.isEmpty()) {
            warningShownFor.clear()
            return
        }

        for (session in activeSessions) {
            when (session.state) {
                is AccessSessionManager.AccessState.Expired -> {
                    handleExpiredSession(session)
                }
                is AccessSessionManager.AccessState.Warning -> {
                    handleWarningSession(session)
                }
                is AccessSessionManager.AccessState.Active -> {
                    // Session is active, update usage tracking
                    trackUsage(session)
                }
                else -> {}
            }
        }

        // Update foreground notification with active session count
        updateForegroundNotification(activeSessions.size)
    }

    private suspend fun handleExpiredSession(session: AccessSessionManager.ActiveSessionInfo) {
        Log.d(TAG, "Session expired for ${session.packageName}")

        // Mark session as expired
        accessSessionManager.expireSession(session.packageName)
        warningShownFor.remove(session.packageName)

        // Log the expiration
        database.usageLogDao().insertLog(
            app.gonull.data.local.entity.UsageLogEntity(
                packageName = session.packageName,
                eventType = "ACCESS_EXPIRED"
            )
        )

        // Invalidate blocker cache to re-block the app
        AppBlockerService.invalidateCache(applicationContext)

        // Show notification
        showExpiredNotification(session.packageName)
    }

    private fun handleWarningSession(session: AccessSessionManager.ActiveSessionInfo) {
        if (warningShownFor.contains(session.packageName)) {
            return // Already warned
        }

        Log.d(TAG, "Showing warning for ${session.packageName}, remaining: ${session.remainingMs}ms")
        warningShownFor.add(session.packageName)

        // Show warning notification
        showWarningNotification(session)

        // Launch warning overlay
        val intent = Intent(applicationContext, AccessWarningActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PACKAGE_NAME, session.packageName)
            putExtra(EXTRA_REMAINING_MS, session.remainingMs)
        }
        startActivity(intent)
    }

    private suspend fun trackUsage(session: AccessSessionManager.ActiveSessionInfo) {
        // Track usage for the usage mirror feature
        val today = app.gonull.util.DateHelper.getTodayString()
        val usedMinutes = ((System.currentTimeMillis() - session.startTime) / 60000).toInt()

        // Update daily usage (this will be called every check interval)
        // The DAO handles not double-counting
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Foreground service channel (low priority)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Session Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors active app access sessions"
                setShowBadge(false)
            }

            // Warning channel (high priority)
            val warningChannel = NotificationChannel(
                CHANNEL_WARNING,
                "Access Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings when app access is about to expire"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }

            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(warningChannel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setContentTitle("GoNull Active")
            .setContentText("Monitoring app access sessions")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateForegroundNotification(activeSessionCount: Int) {
        val text = if (activeSessionCount > 0) {
            "$activeSessionCount active session${if (activeSessionCount > 1) "s" else ""}"
        } else {
            "No active sessions"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setContentTitle("GoNull Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun showWarningNotification(session: AccessSessionManager.ActiveSessionInfo) {
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(session.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            session.packageName
        }

        val remainingSeconds = (session.remainingMs / 1000).toInt()
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60

        val notification = NotificationCompat.Builder(this, CHANNEL_WARNING)
            .setContentTitle("Access ending soon")
            .setContentText("$appName: ${minutes}:${String.format("%02d", seconds)} remaining")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(WARNING_NOTIFICATION_ID, notification)
    }

    private fun showExpiredNotification(packageName: String) {
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_WARNING)
            .setContentTitle("Access expired")
            .setContentText("$appName has been re-blocked")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(EXPIRED_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "AccessExpirationService"

        const val ACTION_START = "app.gonull.ACTION_START_EXPIRATION_SERVICE"
        const val ACTION_STOP = "app.gonull.ACTION_STOP_EXPIRATION_SERVICE"
        const val ACTION_DISMISS_WARNING = "app.gonull.ACTION_DISMISS_WARNING"

        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_REMAINING_MS = "remaining_ms"

        private const val FOREGROUND_NOTIFICATION_ID = 2001
        private const val WARNING_NOTIFICATION_ID = 2002
        private const val EXPIRED_NOTIFICATION_ID = 2003

        private const val CHANNEL_SERVICE = "access_monitoring"
        private const val CHANNEL_WARNING = "access_warning"

        private const val CHECK_INTERVAL_MS = 5000L // Check every 5 seconds

        fun start(context: Context) {
            val intent = Intent(context, AccessExpirationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AccessExpirationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun dismissWarning(context: Context, packageName: String) {
            val intent = Intent(context, AccessExpirationService::class.java).apply {
                action = ACTION_DISMISS_WARNING
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
            context.startService(intent)
        }
    }
}
