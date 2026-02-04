package app.gonull.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "daily_bypass_counts",
    primaryKeys = ["packageName", "date"]
)
data class DailyBypassCountEntity(
    val packageName: String,
    val date: String, // YYYY-MM-DD format
    val bypassCount: Int = 0,
    val lastBypassAt: Long = System.currentTimeMillis()
)
