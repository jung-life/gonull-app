package app.gonull.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.gonull.R
import app.gonull.data.local.AppDatabase
import app.gonull.ui.MainActivity
import app.gonull.util.Constants
import kotlinx.coroutines.*

class UnlockTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME") ?: return START_NOT_STICKY
        val unlockTime = intent.getLongExtra("UNLOCK_TIME", 0L)

        startForeground(NOTIFICATION_ID, createNotification(packageName, unlockTime))

        serviceScope.launch {
            monitorUnlock(packageName, unlockTime)
        }

        return START_STICKY
    }

    private suspend fun monitorUnlock(packageName: String, unlockTime: Long) {
        while (System.currentTimeMillis() < unlockTime) {
            val remaining = unlockTime - System.currentTimeMillis()
            updateNotification(packageName, remaining)
            delay(1000)
        }

        // Unlock complete
        database.unlockRequestDao().getPendingRequest(packageName)?.let { request ->
            database.unlockRequestDao().updateStatus(request.id, Constants.RequestStatus.UNLOCKED)
        }

        showUnlockCompleteNotification(packageName)
        stopSelf()
    }

    private fun createNotification(packageName: String, unlockTime: Long): Notification {
        val remaining = unlockTime - System.currentTimeMillis()
        val minutes = (remaining / 60000).toInt()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.TIMER_CHANNEL_ID)
            .setContentTitle("Unlock in progress")
            .setContentText("$packageName unlocks in $minutes minutes")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(packageName: String, remaining: Long) {
        val minutes = (remaining / 60000).toInt()
        val seconds = ((remaining % 60000) / 1000).toInt()

        val notification = NotificationCompat.Builder(this, Constants.TIMER_CHANNEL_ID)
            .setContentTitle("Unlock in progress")
            .setContentText("$packageName unlocks in ${minutes}m ${seconds}s")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun showUnlockCompleteNotification(packageName: String) {
        val notification = NotificationCompat.Builder(this, Constants.TIMER_CHANNEL_ID)
            .setContentTitle("App unlocked")
            .setContentText("$packageName is now accessible for 15 minutes")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.TIMER_CHANNEL_ID,
            "Unlock Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows unlock countdown"
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
        private const val NOTIFICATION_ID = 1001

        fun startTimer(context: Context, packageName: String, unlockTime: Long) {
            val intent = Intent(context, UnlockTimerService::class.java).apply {
                putExtra("PACKAGE_NAME", packageName)
                putExtra("UNLOCK_TIME", unlockTime)
            }
            context.startForegroundService(intent)
        }
    }
}
