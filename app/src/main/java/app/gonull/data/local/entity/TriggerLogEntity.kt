package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_logs")
data class TriggerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val trigger: String, // BORED, ANXIOUS, LONELY, PROCRASTINATING, AUTOPILOT
    val cravingIntensity: Int, // 1-5
    val timestamp: Long = System.currentTimeMillis()
)
