package app.gonull.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin receiver for Lock Mode.
 *
 * While active, removing GoNull goes through a cooldown instead of an impulse
 * tap — a deliberate speed bump, not a hard block. The user can cancel the
 * cooldown, and can always disable Device Admin in system settings.
 */
class GoNullDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "Lock Mode on — removing GoNull now goes through a cooldown",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "Lock Mode off",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Turning off Lock Mode removes the removal cooldown, so you can uninstall GoNull right away. Continue?"
    }
}
