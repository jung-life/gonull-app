package app.gonull.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin receiver for anti-uninstall protection.
 *
 * When enabled, this prevents the user from uninstalling the app during
 * an active blocking session, providing stronger commitment enforcement.
 *
 * Users can disable Device Admin in Settings after their block session ends.
 */
class GoNullDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "GoNull Lock Mode enabled - App cannot be uninstalled during block sessions",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "GoNull Lock Mode disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling Lock Mode will allow you to uninstall GoNull during active blocks. Are you sure?"
    }
}
