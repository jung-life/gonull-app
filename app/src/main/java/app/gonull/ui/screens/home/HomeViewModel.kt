package app.gonull.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.util.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeViewModel(
    private val database: AppDatabase
) : ViewModel() {

    val blockedApps: StateFlow<List<BlockedAppEntity>> = database.blockedAppDao()
        .getActiveBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingUnlocks: StateFlow<List<UnlockRequestEntity>> = database.unlockRequestDao()
        .getAllPendingRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _blockedCountToday = MutableStateFlow(0)
    val blockedCountToday: Int
        get() = _blockedCountToday.value

    private val _timeSavedMinutes = MutableStateFlow(0)
    val timeSavedMinutes: Int
        get() = _timeSavedMinutes.value

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val todayStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
            val count = database.usageLogDao().getBlockedCountSince(todayStart)
            _blockedCountToday.value = count

            // Estimate time saved: each block saves ~15 minutes of potential scrolling
            // This is a rough estimate based on average session length
            _timeSavedMinutes.value = count * 15
        }
    }

    fun removeBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            database.blockedAppDao().deleteBlockedApp(app)
        }
    }

    fun cancelUnlockRequest(request: UnlockRequestEntity) {
        viewModelScope.launch {
            database.unlockRequestDao().updateStatus(
                request.id,
                Constants.RequestStatus.CANCELLED
            )
        }
    }
}
