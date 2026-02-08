package app.gonull.service

import android.content.Context
import android.content.Intent
import android.util.Log
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.FocusModeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages focus modes (Gym Mode, Meditation Mode, Analog Mode) that allow certain apps
 * to bypass blocking during specific activities.
 */
class FocusModeManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val focusModeDao = database.focusModeDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "FocusModeManager"
        const val ACTION_FOCUS_MODE_CHANGED = "app.gonull.ACTION_FOCUS_MODE_CHANGED"

        @Volatile
        private var INSTANCE: FocusModeManager? = null

        fun getInstance(context: Context): FocusModeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FocusModeManager(
                    context.applicationContext,
                    AppDatabase.getDatabase(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    init {
        // Initialize default focus modes if they don't exist
        scope.launch {
            initializeDefaultModes()
        }
    }

    private suspend fun initializeDefaultModes() {
        // Create Gym Mode if not exists
        if (focusModeDao.getMode(FocusModeEntity.TYPE_GYM) == null) {
            focusModeDao.insertMode(FocusModeEntity.createGymMode())
            Log.d(TAG, "Created default Gym Mode")
        }

        // Create Meditation Mode if not exists
        if (focusModeDao.getMode(FocusModeEntity.TYPE_MEDITATION) == null) {
            focusModeDao.insertMode(FocusModeEntity.createMeditationMode())
            Log.d(TAG, "Created default Meditation Mode")
        }

        // Create Analog Mode if not exists
        if (focusModeDao.getMode(FocusModeEntity.TYPE_ANALOG) == null) {
            focusModeDao.insertMode(FocusModeEntity.createAnalogMode())
            Log.d(TAG, "Created default Analog Mode")
        }
    }

    /**
     * Activate a focus mode
     * @param modeType The type of focus mode (GYM, MEDITATION, or ANALOG)
     * @param durationMinutes Duration in minutes, null for indefinite
     */
    suspend fun activateFocusMode(modeType: String, durationMinutes: Int? = null) {
        val now = System.currentTimeMillis()
        val expiresAt = durationMinutes?.let { now + (it * 60 * 1000L) }

        focusModeDao.setModeActive(modeType, true, now, expiresAt)
        Log.d(TAG, "Activated $modeType mode, expires at: $expiresAt")

        // Notify the blocker service
        notifyFocusModeChanged()
    }

    /**
     * Deactivate a focus mode
     */
    suspend fun deactivateFocusMode(modeType: String) {
        focusModeDao.setModeActive(modeType, false, null, null)
        Log.d(TAG, "Deactivated $modeType mode")

        // Notify the blocker service
        notifyFocusModeChanged()
    }

    /**
     * Toggle a focus mode
     */
    suspend fun toggleFocusMode(modeType: String, durationMinutes: Int? = null): Boolean {
        val mode = focusModeDao.getMode(modeType)
        return if (mode?.isActive == true) {
            deactivateFocusMode(modeType)
            false
        } else {
            activateFocusMode(modeType, durationMinutes)
            true
        }
    }

    /**
     * Check if a package is allowed by any active focus mode
     */
    suspend fun isPackageAllowedByFocusMode(packageName: String): Boolean {
        // First, deactivate any expired modes
        focusModeDao.deactivateExpiredModes(System.currentTimeMillis())

        return focusModeDao.isPackageAllowedByActiveFocusMode(
            packageName,
            System.currentTimeMillis()
        )
    }

    /**
     * Get all active focus modes
     */
    suspend fun getActiveModes(): List<FocusModeEntity> {
        // Deactivate expired modes first
        focusModeDao.deactivateExpiredModes(System.currentTimeMillis())
        return focusModeDao.getActiveModes()
    }

    /**
     * Get flow of active modes for UI observation
     */
    fun getActiveModesFlow(): Flow<List<FocusModeEntity>> {
        return focusModeDao.getActiveModesFlow()
    }

    /**
     * Get flow of all modes for settings UI
     */
    fun getAllModesFlow(): Flow<List<FocusModeEntity>> {
        return focusModeDao.getAllModes()
    }

    /**
     * Check if a specific mode is active
     */
    suspend fun isModeActive(modeType: String): Boolean {
        val mode = focusModeDao.getMode(modeType)
        if (mode == null || !mode.isActive) return false

        // Check if expired
        val expiresAt = mode.expiresAt
        if (expiresAt != null && expiresAt <= System.currentTimeMillis()) {
            deactivateFocusMode(modeType)
            return false
        }

        return true
    }

    /**
     * Check if Analog Mode is currently active
     */
    suspend fun isAnalogModeActive(): Boolean {
        return isModeActive(FocusModeEntity.TYPE_ANALOG)
    }

    /**
     * Get the whitelist of packages allowed during Analog Mode
     */
    suspend fun getAnalogWhitelist(): Set<String> {
        val mode = focusModeDao.getMode(FocusModeEntity.TYPE_ANALOG) ?: return emptySet()
        return mode.getAllowedPackagesList().toSet()
    }

    /**
     * Get mode details
     */
    suspend fun getMode(modeType: String): FocusModeEntity? {
        return focusModeDao.getMode(modeType)
    }

    /**
     * Update the allowed packages for a focus mode
     */
    suspend fun updateAllowedPackages(modeType: String, packages: List<String>) {
        focusModeDao.updateAllowedPackages(modeType, packages.joinToString(","))
        notifyFocusModeChanged()
    }

    /**
     * Get remaining time for an active mode in milliseconds
     */
    suspend fun getRemainingTime(modeType: String): Long? {
        val mode = focusModeDao.getMode(modeType) ?: return null
        if (!mode.isActive) return null

        val expiresAt = mode.expiresAt ?: return null // null means indefinite
        val remaining = expiresAt - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }

    /**
     * Get the activatedAt timestamp for an active mode
     */
    suspend fun getActivatedAt(modeType: String): Long? {
        val mode = focusModeDao.getMode(modeType) ?: return null
        if (!mode.isActive) return null
        return mode.activatedAt
    }

    private fun notifyFocusModeChanged() {
        val intent = Intent(ACTION_FOCUS_MODE_CHANGED).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)

        // Also invalidate the blocker cache
        AppBlockerService.invalidateCache(context)
    }
}
