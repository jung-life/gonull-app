package app.gonull.util

import android.content.Context

/**
 * Helper object for managing app preferences using SharedPreferences.
 */
object PreferenceHelper {
    private const val PREFS_NAME = "gonull_preferences"
    private const val KEY_INITIAL_SETUP_COMPLETE = "has_completed_initial_setup"
    private const val KEY_BOREDOM_BEFORE_UNLOCK = "boredom_before_unlock_enabled"

    fun isInitialSetupComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_INITIAL_SETUP_COMPLETE, false)
    }

    fun setInitialSetupComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_COMPLETE, true).apply()
    }

    fun isBoredomBeforeUnlockEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BOREDOM_BEFORE_UNLOCK, false)
    }

    fun setBoredomBeforeUnlock(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BOREDOM_BEFORE_UNLOCK, enabled).apply()
    }
}
