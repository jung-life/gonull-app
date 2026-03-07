package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_session_reflections")
data class PostSessionReflectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val worthItRating: Int, // 1-5
    val reflectionText: String? = null,
    val sessionDurationMinutes: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
