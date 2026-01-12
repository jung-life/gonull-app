package app.gonull.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This receiver can be used for scheduled tasks
        // For Phase 1, keeping it minimal
    }
}
