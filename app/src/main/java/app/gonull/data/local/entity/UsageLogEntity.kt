package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val eventType: String, // BLOCKED, UNLOCKED, USED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long? = null
)
