package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks daily streaks for apps.
 * A streak represents consecutive days without using (or staying under budget for) an app.
 */
@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val packageName: String,

    /** Current streak count (consecutive days) */
    val currentStreak: Int = 0,

    /** Longest streak ever achieved */
    val longestStreak: Int = 0,

    /** Last date the streak was updated (YYYY-MM-DD) */
    val lastUpdatedDate: String,

    /** Date when current streak started (YYYY-MM-DD) */
    val streakStartDate: String,

    /** Total days tracked */
    val totalDaysTracked: Int = 0,

    /** Total "clean" days (no usage or under budget) */
    val totalCleanDays: Int = 0
)
