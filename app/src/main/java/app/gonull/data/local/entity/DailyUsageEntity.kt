package app.gonull.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "daily_usage",
    primaryKeys = ["packageName", "date"]
)
data class DailyUsageEntity(
    val packageName: String,
    val date: String, // YYYY-MM-DD format
    val usedMinutes: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
