package app.gonull.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import app.gonull.receiver.GoNullDeviceAdminReceiver

/**
 * Helper class for managing Device Admin features.
 * Used for "Lock Mode" which prevents app uninstallation during blocking sessions.
 */
object DeviceAdminHelper {

    /**
     * Check if Device Admin is currently enabled for GoNull.
     */
    fun isDeviceAdminEnabled(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, GoNullDeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(componentName)
    }

    /**
     * Request Device Admin privileges from the user.
     * This will open the system dialog for enabling Device Admin.
     */
    fun requestDeviceAdmin(context: Context) {
        val componentName = ComponentName(context, GoNullDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable Lock Mode to prevent uninstalling GoNull during active block sessions. " +
                        "This helps maintain your commitment. You can disable this after your session ends."
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Remove Device Admin privileges.
     * Note: This can only be done programmatically by the app itself.
     * Users can also manually remove it in Settings > Security > Device Admins.
     */
    fun removeDeviceAdmin(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, GoNullDeviceAdminReceiver::class.java)

        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.removeActiveAdmin(componentName)
        }
    }

    /**
     * Open the Device Admin settings page where users can manually disable.
     */
    fun openDeviceAdminSettings(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
