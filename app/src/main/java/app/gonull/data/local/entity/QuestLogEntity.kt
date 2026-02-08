package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_log")
data class QuestLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questId: String,
    val questTitle: String,
    val questCategory: String,
    val completedAt: Long = System.currentTimeMillis(),
    val triggeredByPackage: String? = null
)
