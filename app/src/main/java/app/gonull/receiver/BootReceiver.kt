package app.gonull.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.gonull.data.local.AppDatabase
import app.gonull.service.AccessExpirationService
import app.gonull.service.FocusModeManager
import app.gonull.service.UnlockTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pending = goAsync()
        val database = AppDatabase.getDatabase(context)
        val focusModeManager = FocusModeManager.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()

                // Drop any pending requests whose timer would already have fired.
                database.unlockRequestDao().expireOldRequests(now)

                // Touching getActiveModes() runs the expired-mode sweep inside FocusModeManager.
                focusModeManager.getActiveModes()

                val hasBlockedApps = database.blockedAppDao()
                    .getActiveBlockedApps()
                    .first()
                    .isNotEmpty()

                if (hasBlockedApps) {
                    AccessExpirationService.start(context)
                }

                // Re-arm the unlock countdown for the soonest-firing pending request.
                // UnlockTimerService is a singleton with one notification slot, so we
                // restart only the most-imminent one rather than all pending requests.
                val nextPending = database.unlockRequestDao()
                    .getAllPendingRequests()
                    .first()
                    .filter { it.unlocksAt > now }
                    .minByOrNull { it.unlocksAt }

                if (nextPending != null) {
                    UnlockTimerService.startTimer(context, nextPending.packageName, nextPending.unlocksAt)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Boot restore failed", t)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
