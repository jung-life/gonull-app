package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val unlockDelayMinutes: Int = 30,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    // Usage budget fields
    val budgetMinutes: Int? = null, // null = unlimited
    val budgetEnabled: Boolean = false
)
