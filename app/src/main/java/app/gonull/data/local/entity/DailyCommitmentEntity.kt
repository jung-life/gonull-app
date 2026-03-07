package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_commitments")
data class DailyCommitmentEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val commitmentText: String,
    val createdAt: Long = System.currentTimeMillis()
)
