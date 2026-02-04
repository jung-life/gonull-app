package app.gonull.service

import android.content.Context
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.UnlockRequestEntity
import app.gonull.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages active access sessions and tracks expiration.
 *
 * Handles:
 * - Checking if an app has active access
 * - Calculating remaining access time
 * - Determining when to show 2-minute warning
 * - Re-blocking apps when access expires
 */
class AccessSessionManager(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val unlockRequestDao = database.unlockRequestDao()

    /**
     * Represents the current state of access for an app
     */
    sealed class AccessState {
        /** App is blocked, no active access */
        object Blocked : AccessState()

        /** Access has expired, should re-block */
        object Expired : AccessState()

        /** Access is active but expiring soon (within WARNING_THRESHOLD) */
        data class Warning(val remainingMs: Long) : AccessState()

        /** Access is active with plenty of time remaining */
        data class Active(val remainingMs: Long) : AccessState()
    }

    /**
     * Data class for active session info
     */
    data class ActiveSessionInfo(
        val packageName: String,
        val startTime: Long,
        val durationMinutes: Int,
        val expiresAt: Long,
        val remainingMs: Long,
        val state: AccessState
    )

    /**
     * Get the current access state for an app
     */
    suspend fun getAccessState(packageName: String): AccessState {
        val activeUnlock = unlockRequestDao.getActiveUnlock(packageName, System.currentTimeMillis())
            ?: return AccessState.Blocked

        return calculateAccessState(activeUnlock)
    }

    /**
     * Get detailed session info for an app
     */
    suspend fun getActiveSessionInfo(packageName: String): ActiveSessionInfo? {
        val activeUnlock = unlockRequestDao.getActiveUnlock(packageName, System.currentTimeMillis())
            ?: return null

        val expiresAt = activeUnlock.unlocksAt + (activeUnlock.accessDurationMinutes * 60 * 1000L)
        val remainingMs = expiresAt - System.currentTimeMillis()
        val state = calculateAccessState(activeUnlock)

        return ActiveSessionInfo(
            packageName = activeUnlock.packageName,
            startTime = activeUnlock.unlocksAt,
            durationMinutes = activeUnlock.accessDurationMinutes,
            expiresAt = expiresAt,
            remainingMs = remainingMs.coerceAtLeast(0),
            state = state
        )
    }

    /**
     * Get all sessions that are in warning state (expiring within 2 minutes)
     */
    suspend fun getExpiringSessions(): List<ActiveSessionInfo> {
        val now = System.currentTimeMillis()
        val warningThreshold = now + WARNING_THRESHOLD_MS

        return unlockRequestDao.getSessionsExpiringSoon(now, warningThreshold)
            .mapNotNull { unlock ->
                val expiresAt = unlock.unlocksAt + (unlock.accessDurationMinutes * 60 * 1000L)
                val remainingMs = expiresAt - now

                if (remainingMs > 0 && remainingMs <= WARNING_THRESHOLD_MS) {
                    ActiveSessionInfo(
                        packageName = unlock.packageName,
                        startTime = unlock.unlocksAt,
                        durationMinutes = unlock.accessDurationMinutes,
                        expiresAt = expiresAt,
                        remainingMs = remainingMs,
                        state = AccessState.Warning(remainingMs)
                    )
                } else null
            }
    }

    /**
     * Get all currently active sessions (for monitoring)
     */
    suspend fun getAllActiveSessions(): List<ActiveSessionInfo> {
        val now = System.currentTimeMillis()
        return unlockRequestDao.getAllActiveUnlocks(now).map { unlock ->
            val expiresAt = unlock.unlocksAt + (unlock.accessDurationMinutes * 60 * 1000L)
            val remainingMs = expiresAt - now
            ActiveSessionInfo(
                packageName = unlock.packageName,
                startTime = unlock.unlocksAt,
                durationMinutes = unlock.accessDurationMinutes,
                expiresAt = expiresAt,
                remainingMs = remainingMs.coerceAtLeast(0),
                state = calculateAccessState(unlock)
            )
        }
    }

    /**
     * Mark a session as expired
     */
    suspend fun expireSession(packageName: String) {
        unlockRequestDao.expireSessionForPackage(packageName)
    }

    /**
     * Check if access is about to expire (within warning threshold)
     */
    suspend fun isAccessExpiringSoon(packageName: String): Boolean {
        val state = getAccessState(packageName)
        return state is AccessState.Warning
    }

    /**
     * Get remaining time in a human-readable format
     */
    fun formatRemainingTime(remainingMs: Long): String {
        val totalSeconds = (remainingMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun calculateAccessState(unlock: UnlockRequestEntity): AccessState {
        val now = System.currentTimeMillis()
        val expiresAt = unlock.unlocksAt + (unlock.accessDurationMinutes * 60 * 1000L)
        val remainingMs = expiresAt - now

        return when {
            remainingMs <= 0 -> AccessState.Expired
            remainingMs <= WARNING_THRESHOLD_MS -> AccessState.Warning(remainingMs)
            else -> AccessState.Active(remainingMs)
        }
    }

    companion object {
        /** Warning threshold: 2 minutes before expiration */
        const val WARNING_THRESHOLD_MS = 2 * 60 * 1000L

        private var instance: AccessSessionManager? = null

        fun getInstance(context: Context): AccessSessionManager {
            return instance ?: synchronized(this) {
                instance ?: AccessSessionManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
