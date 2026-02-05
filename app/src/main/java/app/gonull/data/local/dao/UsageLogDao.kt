package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<UsageLogEntity>>

    @Query("SELECT COUNT(*) FROM usage_logs WHERE eventType = 'BLOCKED' AND timestamp >= :startTime")
    suspend fun getBlockedCountSince(startTime: Long): Int

    @Query("SELECT * FROM usage_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getLogsSince(startTime: Long): List<UsageLogEntity>

    @Insert
    suspend fun insertLog(log: UsageLogEntity)

    @Query("DELETE FROM usage_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)

    // Stats queries
    @Query("SELECT COUNT(*) FROM usage_logs WHERE eventType = 'BLOCKED'")
    suspend fun getTotalBlockedCount(): Int

    @Query("SELECT COUNT(*) FROM usage_logs WHERE eventType = 'BYPASS'")
    suspend fun getTotalBypassCount(): Int

    @Query("SELECT COUNT(*) FROM usage_logs WHERE eventType = 'EMERGENCY_UNLOCK'")
    suspend fun getTotalEmergencyCount(): Int

    @Query("SELECT COUNT(*) FROM usage_logs WHERE eventType = 'BLOCKED' AND timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getBlockedCountBetween(startTime: Long, endTime: Long): Int

    @Query("SELECT packageName, COUNT(*) as count FROM usage_logs WHERE eventType = 'BLOCKED' GROUP BY packageName ORDER BY count DESC LIMIT 5")
    suspend fun getTopBlockedApps(): List<AppBlockCount>

    @Query("SELECT * FROM usage_logs WHERE eventType IN ('BLOCKED', 'BYPASS', 'EMERGENCY_UNLOCK') ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentActivity(limit: Int): List<UsageLogEntity>

    // Get blocks per day for the last N days
    @Query("""
        SELECT
            date(timestamp/1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as count
        FROM usage_logs
        WHERE eventType = 'BLOCKED' AND timestamp >= :startTime
        GROUP BY date(timestamp/1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    suspend fun getBlocksPerDay(startTime: Long): List<DayBlockCount>
}

data class AppBlockCount(
    val packageName: String,
    val count: Int
)

data class DayBlockCount(
    val date: String,
    val count: Int
)
