package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.DailyUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {
    @Query("SELECT * FROM daily_usage WHERE packageName = :packageName AND date = :date")
    suspend fun getUsage(packageName: String, date: String): DailyUsageEntity?

    @Query("SELECT usedMinutes FROM daily_usage WHERE packageName = :packageName AND date = :date")
    suspend fun getUsedMinutes(packageName: String, date: String): Int?

    @Query("SELECT * FROM daily_usage WHERE date = :date")
    fun getUsageForDate(date: String): Flow<List<DailyUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: DailyUsageEntity)

    @Query("UPDATE daily_usage SET usedMinutes = usedMinutes + :minutes, lastUpdatedAt = :timestamp WHERE packageName = :packageName AND date = :date")
    suspend fun addMinutes(packageName: String, date: String, minutes: Int, timestamp: Long = System.currentTimeMillis()): Int

    @Transaction
    suspend fun addOrCreateUsage(packageName: String, date: String, minutes: Int): Int {
        val updated = addMinutes(packageName, date, minutes)
        if (updated == 0) {
            insert(DailyUsageEntity(packageName = packageName, date = date, usedMinutes = minutes))
            return minutes
        }
        return (getUsedMinutes(packageName, date) ?: minutes)
    }

    @Query("DELETE FROM daily_usage WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)

    /**
     * Get total usage for a package over a date range
     */
    @Query("SELECT SUM(usedMinutes) FROM daily_usage WHERE packageName = :packageName AND date >= :startDate AND date <= :endDate")
    suspend fun getUsageForDateRange(packageName: String, startDate: String, endDate: String): Int?

    /**
     * Get total usage across all apps for a date
     */
    @Query("SELECT SUM(usedMinutes) FROM daily_usage WHERE date = :date")
    suspend fun getTotalUsageForDate(date: String): Int?

    /**
     * Get total usage across all apps for a date range
     */
    @Query("SELECT SUM(usedMinutes) FROM daily_usage WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalUsageForDateRange(startDate: String, endDate: String): Int?

    /**
     * Get usage breakdown by app for a date range
     */
    @Query("""
        SELECT packageName, SUM(usedMinutes) as usedMinutes, MAX(date) as date, MAX(lastUpdatedAt) as lastUpdatedAt
        FROM daily_usage
        WHERE date >= :startDate AND date <= :endDate
        GROUP BY packageName
        ORDER BY usedMinutes DESC
    """)
    suspend fun getUsageBreakdown(startDate: String, endDate: String): List<DailyUsageEntity>
}
