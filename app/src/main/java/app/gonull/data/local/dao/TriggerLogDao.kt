package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.TriggerLogEntity

@Dao
interface TriggerLogDao {
    @Insert
    suspend fun insertTrigger(triggerLog: TriggerLogEntity)

    @Query("SELECT trigger, COUNT(*) as count FROM trigger_logs GROUP BY trigger ORDER BY count DESC")
    suspend fun getTriggerCounts(): List<TriggerCount>

    @Query("SELECT AVG(cravingIntensity) FROM trigger_logs")
    suspend fun getAverageCravingIntensity(): Float?

    @Query("SELECT trigger, AVG(cravingIntensity) as avgIntensity FROM trigger_logs GROUP BY trigger")
    suspend fun getCravingByTrigger(): List<TriggerCraving>
}

data class TriggerCount(
    val trigger: String,
    val count: Int
)

data class TriggerCraving(
    val trigger: String,
    val avgIntensity: Float
)
