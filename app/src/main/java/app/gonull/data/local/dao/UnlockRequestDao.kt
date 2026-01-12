package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.UnlockRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockRequestDao {
    @Query("SELECT * FROM unlock_requests WHERE packageName = :packageName AND status = 'PENDING' ORDER BY requestedAt DESC LIMIT 1")
    suspend fun getPendingRequest(packageName: String): UnlockRequestEntity?

    @Query("SELECT * FROM unlock_requests WHERE status = 'PENDING'")
    fun getAllPendingRequests(): Flow<List<UnlockRequestEntity>>

    @Query("SELECT * FROM unlock_requests WHERE packageName = :packageName AND status = 'UNLOCKED' AND unlocksAt + (accessDurationMinutes * 60 * 1000) > :currentTime")
    suspend fun getActiveUnlock(packageName: String, currentTime: Long): UnlockRequestEntity?

    @Insert
    suspend fun insertRequest(request: UnlockRequestEntity): Long

    @Query("UPDATE unlock_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE unlock_requests SET status = 'EXPIRED' WHERE status = 'PENDING' AND unlocksAt < :currentTime")
    suspend fun expireOldRequests(currentTime: Long)
}
