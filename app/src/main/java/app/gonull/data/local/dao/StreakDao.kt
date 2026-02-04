package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streaks WHERE packageName = :packageName")
    suspend fun getStreak(packageName: String): StreakEntity?

    @Query("SELECT * FROM streaks WHERE packageName = :packageName")
    fun observeStreak(packageName: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks ORDER BY currentStreak DESC")
    fun observeAllStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks ORDER BY currentStreak DESC")
    suspend fun getAllStreaks(): List<StreakEntity>

    @Query("SELECT SUM(currentStreak) FROM streaks")
    suspend fun getTotalStreakDays(): Int?

    @Query("SELECT MAX(longestStreak) FROM streaks")
    suspend fun getOverallLongestStreak(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: StreakEntity)

    @Query("""
        UPDATE streaks
        SET currentStreak = currentStreak + 1,
            longestStreak = CASE WHEN currentStreak + 1 > longestStreak THEN currentStreak + 1 ELSE longestStreak END,
            lastUpdatedDate = :date,
            totalDaysTracked = totalDaysTracked + 1,
            totalCleanDays = totalCleanDays + 1
        WHERE packageName = :packageName
    """)
    suspend fun incrementStreak(packageName: String, date: String): Int

    @Query("""
        UPDATE streaks
        SET currentStreak = 0,
            streakStartDate = :date,
            lastUpdatedDate = :date,
            totalDaysTracked = totalDaysTracked + 1
        WHERE packageName = :packageName
    """)
    suspend fun resetStreak(packageName: String, date: String): Int

    @Transaction
    suspend fun recordCleanDay(packageName: String, date: String) {
        val existing = getStreak(packageName)
        if (existing == null) {
            // First time tracking this app
            upsert(StreakEntity(
                packageName = packageName,
                currentStreak = 1,
                longestStreak = 1,
                lastUpdatedDate = date,
                streakStartDate = date,
                totalDaysTracked = 1,
                totalCleanDays = 1
            ))
        } else if (existing.lastUpdatedDate != date) {
            // New day, increment streak
            incrementStreak(packageName, date)
        }
        // Same day, do nothing
    }

    @Transaction
    suspend fun recordUsageDay(packageName: String, date: String) {
        val existing = getStreak(packageName)
        if (existing == null) {
            // First time tracking, start with broken streak
            upsert(StreakEntity(
                packageName = packageName,
                currentStreak = 0,
                longestStreak = 0,
                lastUpdatedDate = date,
                streakStartDate = date,
                totalDaysTracked = 1,
                totalCleanDays = 0
            ))
        } else if (existing.lastUpdatedDate != date) {
            // New day with usage, reset streak
            resetStreak(packageName, date)
        }
        // Same day, already recorded
    }

    @Query("DELETE FROM streaks WHERE packageName = :packageName")
    suspend fun deleteStreak(packageName: String)
}
