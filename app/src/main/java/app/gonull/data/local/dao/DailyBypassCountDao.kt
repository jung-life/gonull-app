package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.DailyBypassCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyBypassCountDao {
    @Query("SELECT * FROM daily_bypass_counts WHERE packageName = :packageName AND date = :date")
    suspend fun getBypassCount(packageName: String, date: String): DailyBypassCountEntity?

    @Query("SELECT bypassCount FROM daily_bypass_counts WHERE packageName = :packageName AND date = :date")
    suspend fun getBypassCountValue(packageName: String, date: String): Int?

    @Query("SELECT SUM(bypassCount) FROM daily_bypass_counts WHERE date = :date")
    suspend fun getTotalBypassesForDate(date: String): Int?

    @Query("SELECT * FROM daily_bypass_counts WHERE date = :date")
    fun getBypassCountsForDate(date: String): Flow<List<DailyBypassCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bypassCount: DailyBypassCountEntity)

    @Query("UPDATE daily_bypass_counts SET bypassCount = bypassCount + 1, lastBypassAt = :timestamp WHERE packageName = :packageName AND date = :date")
    suspend fun incrementBypassCount(packageName: String, date: String, timestamp: Long = System.currentTimeMillis()): Int

    @Transaction
    suspend fun incrementOrCreate(packageName: String, date: String): Int {
        val updated = incrementBypassCount(packageName, date)
        if (updated == 0) {
            insert(DailyBypassCountEntity(packageName = packageName, date = date, bypassCount = 1))
            return 1
        }
        return (getBypassCountValue(packageName, date) ?: 1)
    }

    @Query("DELETE FROM daily_bypass_counts WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}
