package app.gonull.service

import android.content.Context

/**
 * Defines different strategies for blocking apps.
 * This allows the app to switch between methods based on:
 * - Google Play policy compliance
 * - User preference
 * - Device capabilities
 */
sealed class BlockingStrategy {
    /**
     * Uses AccessibilityService to detect app launches in real-time.
     * Pros: Instant blocking, best UX
     * Cons: Requires special permission, Google Play policy risk
     */
    object AccessibilityBased : BlockingStrategy()

    /**
     * Uses UsageStatsManager with polling to detect foreground app.
     * Pros: Google Play compliant, no special permissions
     * Cons: 2-second detection lag, higher battery usage
     */
    object UsageStatsBased : BlockingStrategy()

    /**
     * Uses local VPN to block network traffic to specific apps.
     * Pros: Can block background usage, Google Play compliant
     * Cons: More complex, only blocks network-dependent apps
     * Note: Planned for Phase 3
     */
    object VPNBased : BlockingStrategy()
}

/**
 * Manager class that initializes and switches between blocking strategies.
 */
class BlockingStrategyManager(private val context: Context) {

    private var currentStrategy: BlockingStrategy = BlockingStrategy.AccessibilityBased

    /**
     * Set the active blocking strategy.
     * This will stop the current strategy and start the new one.
     */
    fun setStrategy(strategy: BlockingStrategy) {
        stopCurrentStrategy()
        currentStrategy = strategy
        startStrategy(strategy)
    }

    /**
     * Get the currently active strategy.
     */
    fun getCurrentStrategy(): BlockingStrategy = currentStrategy

    private fun startStrategy(strategy: BlockingStrategy) {
        when (strategy) {
            is BlockingStrategy.AccessibilityBased -> {
                // AccessibilityService is started by the system when enabled
                // No explicit start needed here
            }
            is BlockingStrategy.UsageStatsBased -> {
                // Start UsageStatsPollingService
                val intent = android.content.Intent(context, UsageStatsPollingService::class.java)
                context.startForegroundService(intent)
            }
            is BlockingStrategy.VPNBased -> {
                // TODO: Implement VPN-based blocking in Phase 3
                throw NotImplementedError("VPN-based blocking not yet implemented")
            }
        }
    }

    private fun stopCurrentStrategy() {
        when (currentStrategy) {
            is BlockingStrategy.AccessibilityBased -> {
                // AccessibilityService can only be stopped by user in settings
                // No explicit stop needed here
            }
            is BlockingStrategy.UsageStatsBased -> {
                // Stop UsageStatsPollingService
                val intent = android.content.Intent(context, UsageStatsPollingService::class.java)
                context.stopService(intent)
            }
            is BlockingStrategy.VPNBased -> {
                // TODO: Implement VPN stop in Phase 3
            }
        }
    }

    /**
     * Check if the current strategy is available and properly configured.
     */
    fun isStrategyAvailable(strategy: BlockingStrategy): Boolean {
        return when (strategy) {
            is BlockingStrategy.AccessibilityBased -> {
                app.gonull.util.PermissionHelper.isAccessibilityServiceEnabled(context)
            }
            is BlockingStrategy.UsageStatsBased -> {
                app.gonull.util.PermissionHelper.hasUsageStatsPermission(context)
            }
            is BlockingStrategy.VPNBased -> {
                false // Not implemented yet
            }
        }
    }
}
