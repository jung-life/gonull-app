package app.gonull.service

import android.content.Context
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UsageLogEntity
import app.gonull.util.Constants
import app.gonull.util.DateHelper

/**
 * Manages progressive friction levels for app bypasses.
 *
 * Bypass # | Requirements                    | Wait Time
 * ---------|--------------------------------|----------
 * 1st      | Code + Math (current)           | None
 * 2nd      | Code + Math + Wait              | 5 minutes
 * 3rd      | Code + Math + Wait + Reflection | 15 minutes
 * 4th+     | LOCKED                          | Until tomorrow
 */
class ProgressiveFrictionManager(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dailyBypassCountDao = database.dailyBypassCountDao()
    private val usageLogDao = database.usageLogDao()

    /**
     * Get the current bypass count for an app today
     */
    suspend fun getBypassCountToday(packageName: String): Int {
        val today = DateHelper.getTodayString()
        return dailyBypassCountDao.getBypassCountValue(packageName, today) ?: 0
    }

    /**
     * Get the friction level for the next bypass attempt
     * Returns the level (1-4) for the NEXT bypass
     */
    suspend fun getNextFrictionLevel(packageName: String): Int {
        val currentCount = getBypassCountToday(packageName)
        return minOf(currentCount + 1, Constants.Friction.LEVEL_4_LOCKED)
    }

    /**
     * Check if the app is locked for today (4+ bypasses)
     */
    suspend fun isLockedForToday(packageName: String): Boolean {
        return getBypassCountToday(packageName) >= Constants.Friction.LEVEL_4_LOCKED - 1
    }

    /**
     * Get the wait time in minutes required for the current friction level
     */
    fun getWaitTimeMinutes(frictionLevel: Int): Int {
        return when (frictionLevel) {
            Constants.Friction.LEVEL_2_BYPASS -> Constants.Friction.LEVEL_2_WAIT_MINUTES
            Constants.Friction.LEVEL_3_BYPASS -> Constants.Friction.LEVEL_3_WAIT_MINUTES
            else -> 0
        }
    }

    /**
     * Check if reflection is required for the current friction level
     */
    fun isReflectionRequired(frictionLevel: Int): Boolean {
        return frictionLevel == Constants.Friction.LEVEL_3_BYPASS
    }

    /**
     * Record a bypass and increment the daily count
     */
    suspend fun recordBypass(packageName: String, reflectionText: String? = null): Int {
        val today = DateHelper.getTodayString()
        val newCount = dailyBypassCountDao.incrementOrCreate(packageName, today)

        // Log the bypass event
        usageLogDao.insertLog(
            UsageLogEntity(
                packageName = packageName,
                eventType = "BYPASS",
                reflectionText = reflectionText
            )
        )

        return newCount
    }

    /**
     * Get time until midnight when the lock resets
     */
    fun getTimeUntilReset(): Long {
        return DateHelper.getMillisUntilMidnight()
    }

    /**
     * Get a formatted time string until reset (e.g., "4h 32m")
     */
    fun getFormattedTimeUntilReset(): String {
        val millis = getTimeUntilReset()
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    /**
     * Get the description for the current friction level
     */
    fun getFrictionDescription(frictionLevel: Int): String {
        return when (frictionLevel) {
            Constants.Friction.LEVEL_1_BYPASS -> "Complete verification to access"
            Constants.Friction.LEVEL_2_BYPASS -> "Complete verification + ${Constants.Friction.LEVEL_2_WAIT_MINUTES} minute wait"
            Constants.Friction.LEVEL_3_BYPASS -> "Complete verification + ${Constants.Friction.LEVEL_3_WAIT_MINUTES} minute wait + reflection"
            Constants.Friction.LEVEL_4_LOCKED -> "Locked until tomorrow"
            else -> "Access denied"
        }
    }

    /**
     * Get the total bypasses across all apps today
     */
    suspend fun getTotalBypassesToday(): Int {
        val today = DateHelper.getTodayString()
        return dailyBypassCountDao.getTotalBypassesForDate(today) ?: 0
    }

    companion object {
        private var instance: ProgressiveFrictionManager? = null

        fun getInstance(context: Context): ProgressiveFrictionManager {
            return instance ?: synchronized(this) {
                instance ?: ProgressiveFrictionManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
