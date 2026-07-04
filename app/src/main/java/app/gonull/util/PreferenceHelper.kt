package app.gonull.util

import android.content.Context

/**
 * Helper object for managing app preferences using SharedPreferences.
 */
object PreferenceHelper {
    private const val PREFS_NAME = "gonull_preferences"
    private const val KEY_INITIAL_SETUP_COMPLETE = "has_completed_initial_setup"
    private const val KEY_BOREDOM_BEFORE_UNLOCK = "boredom_before_unlock_enabled"
    private const val KEY_REMOVAL_REQUESTED_AT = "removal_requested_at"

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

    /**
     * Timestamp (epoch millis) when the user started the removal cooldown for
     * Lock Mode, or 0L if no removal is pending. The app stays removable only
     * after the cooldown elapses; the user can cancel at any point.
     */
    fun getRemovalRequestedAt(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_REMOVAL_REQUESTED_AT, 0L)
    }

    fun setRemovalRequestedAt(context: Context, timestampMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_REMOVAL_REQUESTED_AT, timestampMillis).apply()
    }

    fun clearRemovalRequest(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_REMOVAL_REQUESTED_AT).apply()
    }
}
