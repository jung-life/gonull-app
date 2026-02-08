package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boredom_sessions")
data class BoredomSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val durationSeconds: Int = 0,
    val wasCompleted: Boolean = false,
    val triggeredByPackage: String? = null,
    val context: String? = null
)
