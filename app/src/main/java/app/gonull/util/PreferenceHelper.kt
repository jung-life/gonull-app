package app.gonull.util

import android.content.Context

/**
 * Helper object for managing app preferences using SharedPreferences.
 * Currently tracks whether the user has completed the initial setup flow.
 */
object PreferenceHelper {
    private const val PREFS_NAME = "gonull_preferences"
    private const val KEY_INITIAL_SETUP_COMPLETE = "has_completed_initial_setup"

    /**
     * Check if the user has completed the initial setup flow.
     *
     * @param context Application context
     * @return true if initial setup is complete, false otherwise
     */
    fun isInitialSetupComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_INITIAL_SETUP_COMPLETE, false)
    }

    /**
     * Mark the initial setup flow as complete.
     * This is called after the user completes the UsageInsightsScreen.
     *
     * @param context Application context
     */
    fun setInitialSetupComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_COMPLETE, true).apply()
    }
}
