package app.gonull.service

import android.content.Context
import android.util.Log
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.PartnerNotificationLogEntity
import app.gonull.util.Constants
import app.gonull.util.DateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service for sending notifications to accountability partners.
 * Notifies partners when the user:
 * - Uses emergency bypass
 * - Has multiple bypasses in a day
 * - Exceeds their usage budget
 */
class AccountabilityNotificationService(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Notify accountability partner about an emergency bypass
     */
    fun notifyEmergencyBypass(packageName: String) {
        scope.launch {
            try {
                val partners = database.accountabilityPartnerDao().getActivePartners()
                if (partners.isEmpty()) return@launch

                val appName = getAppName(packageName)
                val message = "Emergency bypass used for $appName"

                for (partner in partners) {
                    sendNotification(
                        partnerPhone = partner.phoneNumber,
                        notificationType = Constants.NotificationType.EMERGENCY_BYPASS,
                        packageName = packageName,
                        message = message
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying emergency bypass", e)
            }
        }
    }

    /**
     * Check if multiple bypasses have occurred and notify if threshold exceeded
     */
    fun checkAndNotifyMultipleBypasses(packageName: String) {
        scope.launch {
            try {
                val today = DateHelper.getTodayString()
                val bypassCount = database.dailyBypassCountDao().getBypassCountValue(packageName, today) ?: 0

                // Notify on 3rd bypass
                if (bypassCount >= 3) {
                    val partners = database.accountabilityPartnerDao().getActivePartners()
                    if (partners.isEmpty()) return@launch

                    // Check if we already notified today for this app
                    val alreadyNotified = database.partnerNotificationLogDao()
                        .hasNotifiedToday(
                            packageName = packageName,
                            notificationType = Constants.NotificationType.MULTIPLE_BYPASSES,
                            date = today
                        )

                    if (alreadyNotified) return@launch

                    val appName = getAppName(packageName)
                    val message = "$bypassCount bypasses today for $appName"

                    for (partner in partners) {
                        sendNotification(
                            partnerPhone = partner.phoneNumber,
                            notificationType = Constants.NotificationType.MULTIPLE_BYPASSES,
                            packageName = packageName,
                            message = message
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking multiple bypasses", e)
            }
        }
    }

    /**
     * Notify when budget is exceeded
     */
    fun notifyBudgetExceeded(packageName: String) {
        scope.launch {
            try {
                val partners = database.accountabilityPartnerDao().getActivePartners()
                if (partners.isEmpty()) return@launch

                val appName = getAppName(packageName)
                val message = "Daily budget exceeded for $appName"

                for (partner in partners) {
                    sendNotification(
                        partnerPhone = partner.phoneNumber,
                        notificationType = Constants.NotificationType.BUDGET_EXCEEDED,
                        packageName = packageName,
                        message = message
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying budget exceeded", e)
            }
        }
    }

    private suspend fun sendNotification(
        partnerPhone: String,
        notificationType: String,
        packageName: String?,
        message: String
    ) {
        // Log the notification attempt
        val log = PartnerNotificationLogEntity(
            partnerPhone = partnerPhone,
            notificationType = notificationType,
            packageName = packageName,
            message = message,
            sentAt = System.currentTimeMillis(),
            wasSuccessful = true // For now, just log - actual SMS/notification can be implemented later
        )
        database.partnerNotificationLogDao().insert(log)

        // TODO: Implement actual notification sending (SMS, push notification, etc.)
        Log.d(TAG, "Notification to $partnerPhone: $message")
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    companion object {
        private const val TAG = "AccountabilityNotificationService"

        @Volatile
        private var instance: AccountabilityNotificationService? = null

        fun getInstance(context: Context): AccountabilityNotificationService {
            return instance ?: synchronized(this) {
                instance ?: AccountabilityNotificationService(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
