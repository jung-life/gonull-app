package app.gonull.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.gonull.data.local.AppDatabase
import app.gonull.service.AccessExpirationService
import app.gonull.service.UnlockTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                database.unlockRequestDao().expireOldRequests(now)

                // Start access expiration service if there are blocked apps
                val hasBlockedApps = database.blockedAppDao()
                    .getActiveBlockedApps()
                    .first()
                    .isNotEmpty()

                if (hasBlockedApps) {
                    AccessExpirationService.start(context)
                }
            }
        }
    }
}
