package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_requests")
data class UnlockRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val requestedAt: Long,
    val unlocksAt: Long,
    val status: String, // PENDING, UNLOCKED, EXPIRED, CANCELLED
    val accessDurationMinutes: Int = 15
)
