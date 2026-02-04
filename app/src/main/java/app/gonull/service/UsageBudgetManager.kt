package app.gonull.service

import android.content.Context
import app.gonull.data.local.AppDatabase
import app.gonull.util.DateHelper

/**
 * Manages daily usage budgets for blocked apps.
 * Tracks usage time and determines when budget is exhausted.
 */
class UsageBudgetManager(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val blockedAppDao = database.blockedAppDao()
    private val dailyUsageDao = database.dailyUsageDao()

    /**
     * Check if the daily budget for an app is exhausted
     */
    suspend fun isBudgetExhausted(packageName: String): Boolean {
        val blockedApp = blockedAppDao.getBlockedApp(packageName) ?: return false

        // If budget is not enabled, it's never exhausted
        if (!blockedApp.budgetEnabled) return false

        val budgetMinutes = blockedApp.budgetMinutes ?: return false
        val today = DateHelper.getTodayString()
        val usedMinutes = dailyUsageDao.getUsedMinutes(packageName, today) ?: 0

        return usedMinutes >= budgetMinutes
    }

    /**
     * Get remaining budget minutes for an app (null if no budget set)
     */
    suspend fun getRemainingBudgetMinutes(packageName: String): Int? {
        val blockedApp = blockedAppDao.getBlockedApp(packageName) ?: return null

        // If budget is not enabled, return null
        if (!blockedApp.budgetEnabled) return null

        val budgetMinutes = blockedApp.budgetMinutes ?: return null
        val today = DateHelper.getTodayString()
        val usedMinutes = dailyUsageDao.getUsedMinutes(packageName, today) ?: 0

        return (budgetMinutes - usedMinutes).coerceAtLeast(0)
    }

    /**
     * Get the configured budget for an app (null if no budget set)
     */
    suspend fun getBudgetMinutes(packageName: String): Int? {
        val blockedApp = blockedAppDao.getBlockedApp(packageName) ?: return null
        if (!blockedApp.budgetEnabled) return null
        return blockedApp.budgetMinutes
    }

    /**
     * Get today's usage for an app in minutes
     */
    suspend fun getTodayUsageMinutes(packageName: String): Int {
        val today = DateHelper.getTodayString()
        return dailyUsageDao.getUsedMinutes(packageName, today) ?: 0
    }

    /**
     * Record usage time for an app
     */
    suspend fun recordUsage(packageName: String, minutes: Int) {
        val today = DateHelper.getTodayString()
        dailyUsageDao.addOrCreateUsage(packageName, today, minutes)
    }

    /**
     * Check if budget feature is enabled for an app
     */
    suspend fun isBudgetEnabled(packageName: String): Boolean {
        val blockedApp = blockedAppDao.getBlockedApp(packageName) ?: return false
        return blockedApp.budgetEnabled
    }

    companion object {
        @Volatile
        private var instance: UsageBudgetManager? = null

        fun getInstance(context: Context): UsageBudgetManager {
            return instance ?: synchronized(this) {
                instance ?: UsageBudgetManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
