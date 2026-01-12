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
}
