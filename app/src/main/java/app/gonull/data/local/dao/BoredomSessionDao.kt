package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.BoredomSessionEntity

@Dao
interface BoredomSessionDao {
    @Insert
    suspend fun insertSession(session: BoredomSessionEntity): Long

    @Query("UPDATE boredom_sessions SET completedAt = :completedAt, durationSeconds = :durationSeconds, wasCompleted = :wasCompleted WHERE id = :id")
    suspend fun completeSession(id: Long, completedAt: Long, durationSeconds: Int, wasCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM boredom_sessions WHERE wasCompleted = 1")
    suspend fun getTotalCompletedSessions(): Int

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM boredom_sessions WHERE wasCompleted = 1")
    suspend fun getTotalBoredomSeconds(): Int
}
