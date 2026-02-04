package app.gonull

import android.app.Application
import app.gonull.data.local.AppDatabase
import app.gonull.service.AccessExpirationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GoNullApplication : Application() {

    lateinit var database: AppDatabase
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)

        // Start access expiration monitoring if there are blocked apps
        startAccessMonitoringIfNeeded()
    }

    private fun startAccessMonitoringIfNeeded() {
        applicationScope.launch {
            try {
                val hasBlockedApps = database.blockedAppDao()
                    .getActiveBlockedApps()
                    .first()
                    .isNotEmpty()

                if (hasBlockedApps) {
                    AccessExpirationService.start(this@GoNullApplication)
                }
            } catch (e: Exception) {
                // Silently fail on startup - service can be started later
            }
        }
    }

    /**
     * Call this when blocking is enabled or when an unlock is granted
     * to ensure the expiration service is running
     */
    fun ensureAccessMonitoringStarted() {
        AccessExpirationService.start(this)
    }
}
