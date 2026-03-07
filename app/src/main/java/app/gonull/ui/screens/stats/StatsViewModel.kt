package app.gonull.ui.screens.stats

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.dao.TriggerCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class StatsViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _stats = MutableStateFlow(StatsData())
    val stats: StateFlow<StatsData> = _stats.asStateFlow()

    private val _recentActivity = MutableStateFlow<List<ActivityData>>(emptyList())
    val recentActivity: StateFlow<List<ActivityData>> = _recentActivity.asStateFlow()

    private val _topBlockedApps = MutableStateFlow<List<TopBlockedApp>>(emptyList())
    val topBlockedApps: StateFlow<List<TopBlockedApp>> = _topBlockedApps.asStateFlow()

    private val _motivationalMessage = MutableStateFlow(getRandomMotivationalMessage())
    val motivationalMessage: StateFlow<String> = _motivationalMessage.asStateFlow()

    private val _triggerCounts = MutableStateFlow<List<TriggerCount>>(emptyList())
    val triggerCounts: StateFlow<List<TriggerCount>> = _triggerCounts.asStateFlow()

    private val _avgCraving = MutableStateFlow<Float?>(null)
    val avgCraving: StateFlow<Float?> = _avgCraving.asStateFlow()

    private val _avgWorthIt = MutableStateFlow<Float?>(null)
    val avgWorthIt: StateFlow<Float?> = _avgWorthIt.asStateFlow()

    private val _notWorthItCount = MutableStateFlow(0)
    val notWorthItCount: StateFlow<Int> = _notWorthItCount.asStateFlow()

    private val _totalReflections = MutableStateFlow(0)
    val totalReflections: StateFlow<Int> = _totalReflections.asStateFlow()

    fun loadStats(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val usageLogDao = database.usageLogDao()
                val blockedAppDao = database.blockedAppDao()
                val packageManager = context.packageManager

                // Time ranges
                val now = System.currentTimeMillis()
                val todayStart = now - TimeUnit.DAYS.toMillis(1)
                val weekStart = now - TimeUnit.DAYS.toMillis(7)

                // Get stats
                val totalBlocks = usageLogDao.getTotalBlockedCount()
                val todayBlocks = usageLogDao.getBlockedCountSince(todayStart)
                val weekBlocks = usageLogDao.getBlockedCountSince(weekStart)
                val totalBypasses = usageLogDao.getTotalBypassCount()

                // Estimate time saved: 15 min average per block
                val timeSavedMinutes = totalBlocks * 15

                _stats.value = StatsData(
                    totalBlocks = totalBlocks,
                    todayBlocks = todayBlocks,
                    weekBlocks = weekBlocks,
                    totalBypasses = totalBypasses,
                    timeSavedMinutes = timeSavedMinutes
                )

                // Get recent activity
                val recentLogs = usageLogDao.getRecentActivity(20)
                val activityList = recentLogs.map { log ->
                    val appName = try {
                        val appInfo = packageManager.getApplicationInfo(log.packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: PackageManager.NameNotFoundException) {
                        log.packageName.substringAfterLast('.')
                    }

                    ActivityData(
                        appName = appName,
                        type = log.eventType,
                        timestamp = log.timestamp
                    )
                }
                _recentActivity.value = activityList

                // Get top blocked apps
                val topApps = usageLogDao.getTopBlockedApps()
                val topBlockedList = topApps.map { app ->
                    val appName = try {
                        val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: PackageManager.NameNotFoundException) {
                        app.packageName.substringAfterLast('.')
                    }

                    TopBlockedApp(
                        appName = appName,
                        blockCount = app.count
                    )
                }
                _topBlockedApps.value = topBlockedList

                // Update motivational message based on stats
                _motivationalMessage.value = getMotivationalMessage(totalBlocks, todayBlocks)

                // Load trigger pattern stats
                _triggerCounts.value = database.triggerLogDao().getTriggerCounts()
                _avgCraving.value = database.triggerLogDao().getAverageCravingIntensity()

                // Load post-session reflection stats
                _avgWorthIt.value = database.postSessionReflectionDao().getAverageWorthItRating()
                _notWorthItCount.value = database.postSessionReflectionDao().getNotWorthItCount()
                _totalReflections.value = database.postSessionReflectionDao().getTotalReflections()
            }
        }
    }

    private fun getMotivationalMessage(totalBlocks: Int, todayBlocks: Int): String {
        return when {
            totalBlocks == 0 -> "Your journey to digital freedom starts now. Every block is a victory."
            todayBlocks == 0 -> "New day, fresh start. You've blocked ${totalBlocks} distractions so far. Keep going!"
            todayBlocks >= 10 -> "Incredible willpower today! ${todayBlocks} blocks shows real commitment to your goals."
            todayBlocks >= 5 -> "Strong day! You've resisted ${todayBlocks} times today. Your future self thanks you."
            totalBlocks >= 100 -> "A centurion of focus! ${totalBlocks} total blocks. You're building lasting habits."
            totalBlocks >= 50 -> "Halfway to 100! You're proving that change is possible."
            totalBlocks >= 20 -> "You're building momentum. ${totalBlocks} blocks and counting!"
            else -> "Every block is a step toward the life you want. Keep choosing yourself."
        }
    }

    companion object {
        private fun getRandomMotivationalMessage(): String {
            val messages = listOf(
                "Your attention is your most valuable asset. Protect it.",
                "Every moment spent present is a moment well lived.",
                "The real world has more to offer than any feed.",
                "You are not what you scroll. You are what you do.",
                "Freedom is choosing where your attention goes."
            )
            return messages.random()
        }
    }
}
