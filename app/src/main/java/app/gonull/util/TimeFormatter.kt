package app.gonull.util

import java.util.concurrent.TimeUnit

/**
 * Utility object for formatting time values into human-readable strings.
 */
object TimeFormatter {
    /**
     * Format milliseconds into a human-readable usage time string.
     *
     * Examples:
     * - 7200000 ms (2 hours) → "2h 0m"
     * - 5460000 ms (1h 31m) → "1h 31m"
     * - 900000 ms (15 minutes) → "15m"
     * - 30000 ms (30 seconds) → "< 1m"
     *
     * @param millis Time in milliseconds
     * @return Formatted time string (e.g., "2h 34m", "45m", "< 1m")
     */
    fun formatUsageTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
