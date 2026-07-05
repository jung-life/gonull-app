package app.gonull.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object PermissionHelper {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName
        }
    }

    fun openAccessibilitySettings(context: Context) {
        // Try to open directly to the app's accessibility service page
        try {
            val serviceName = ComponentName(context, "app.gonull.service.AppBlockerService")

            // Create intent with extra to highlight our service
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                // Try to pass component name to highlight our service (works on some devices)
                putExtra(":settings:fragment_args_key", serviceName.flattenToString())
                putExtra(":settings:show_fragment_args", android.os.Bundle().apply {
                    putString("component_name", serviceName.flattenToString())
                })
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general accessibility settings
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            context.startActivity(intent)
        }
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback if direct package link fails
            intent.data = null
            context.startActivity(intent)
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Check if Display Over Apps permission is unavailable/disabled on this
     * device (e.g. on Vivo, Oppo, or other OEM devices that disable it).
     * Returns true if the permission setting exists but is toggled off and
     * cannot be enabled (grayed out). This is distinct from canDrawOverlays,
     * which just checks if it's currently granted.
     */
    fun isOverlayPermissionUnavailable(context: Context): Boolean {
        // On devices where it's disabled, we can't check granularly, but we can
        // infer: if the user has Accessibility Service enabled but trying to
        // enable overlay fails repeatedly, it's likely unavailable.
        // For now, we'll rely on the blocking strategy to detect this.
        return false
    }

    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openDeveloperSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Check if a package is a system app or critical service that must never be
     * blockable. This includes launchers, system UI, Google Play Services, Samsung
     * Wallet, etc. — anything that if blocked would make the device unusable.
     */
    fun isSystemOrCriticalService(packageName: String): Boolean {
        return packageName.startsWith("com.android.systemui")
                || packageName.startsWith("com.android.launcher")
                || packageName.startsWith("com.android.settings")
                || packageName.startsWith("com.android.phone")
                || packageName.startsWith("com.android.internal")
                || packageName.startsWith("com.google.android.launcher")
                || packageName.startsWith("com.google.android.apps.nexuslauncher")
                || packageName.startsWith("com.google.android.gms")  // Google Play Services
                || packageName.startsWith("com.google.android.googlequicksearchbox")  // Google Assistant
                || packageName.startsWith("com.google.android.apps.maps")
                // Samsung
                || packageName.startsWith("com.sec.android.app.launcher")
                || packageName.startsWith("com.sec.android.launcher")
                || packageName.startsWith("com.samsung.android.app.wallet")
                || packageName.startsWith("com.samsung.android.app.settings")
                || packageName.startsWith("com.samsung.android.knox")
                || packageName.startsWith("com.samsung.android.messaging")
                || packageName.startsWith("com.samsung.android.dialer")
                || packageName.startsWith("com.samsung.android.contacts")
                || packageName.startsWith("com.samsung.android.spay")
                || packageName.startsWith("com.samsung.samsungpasskey")
                || packageName.startsWith("com.sec.android.inputmethod")
                || packageName.startsWith("com.sec.android.app.phoneprovider")
                // MIUI (Xiaomi)
                || packageName.startsWith("com.miui.home")
                || packageName.startsWith("com.miui.launcher")
                // Other OEM launchers & services (OnePlus, OPPO, Realme, Vivo, Huawei, etc.)
                || packageName.startsWith("com.oneplus.launcher")
                || packageName.startsWith("com.oppo.launcher")
                || packageName.startsWith("com.oppo.cloud")
                || packageName.startsWith("com.realme.launcher")
                || packageName.startsWith("com.vivo.launcher")
                || packageName.startsWith("com.vivo.theme")
                || packageName.startsWith("com.huawei.android.launcher")
                || packageName.startsWith("com.honor.launcher")
                || packageName.startsWith("com.motorola.launcher")
                || packageName.startsWith("com.nothing.launcher")
                || packageName.startsWith("com.teslacoilsw.launcher")
                || packageName.startsWith("com.microsoft.launcher")
                || packageName.startsWith("com.actionlauncher")
                || packageName.startsWith("com.novalauncher")
    }
}
