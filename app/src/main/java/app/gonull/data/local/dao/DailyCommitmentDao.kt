package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.DailyCommitmentEntity

@Dao
interface DailyCommitmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: DailyCommitmentEntity)

    @Query("SELECT * FROM daily_commitments WHERE date = :date")
    suspend fun getCommitmentForDate(date: String): DailyCommitmentEntity?
}
