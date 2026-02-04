package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.UsageSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSessionDao {
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName AND isActive = 1 LIMIT 1")
    suspend fun getActiveSession(packageName: String): UsageSessionEntity?

    @Query("SELECT * FROM usage_sessions WHERE isActive = 1")
    suspend fun getAllActiveSessions(): List<UsageSessionEntity>

    @Query("SELECT * FROM usage_sessions WHERE isActive = 1")
    fun observeActiveSessions(): Flow<List<UsageSessionEntity>>

    @Insert
    suspend fun insert(session: UsageSessionEntity): Long

    @Query("UPDATE usage_sessions SET endedAt = :endTime, isActive = 0 WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTime: Long = System.currentTimeMillis())

    @Query("UPDATE usage_sessions SET endedAt = :endTime, isActive = 0 WHERE packageName = :packageName AND isActive = 1")
    suspend fun endSessionForPackage(packageName: String, endTime: Long = System.currentTimeMillis())

    @Query("UPDATE usage_sessions SET endedAt = :endTime, isActive = 0 WHERE isActive = 1")
    suspend fun endAllActiveSessions(endTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM usage_sessions WHERE startedAt < :cutoffTime")
    suspend fun deleteOldSessions(cutoffTime: Long)
}
