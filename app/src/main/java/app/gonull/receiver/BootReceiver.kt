package app.gonull.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.gonull.data.local.AppDatabase
import app.gonull.service.UnlockTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restart any pending unlock timers
            val database = AppDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                database.unlockRequestDao().expireOldRequests(now)

                // Note: For Phase 1, we'll keep this simple
                // In Phase 2, we can restore active timers
            }
        }
    }
}
