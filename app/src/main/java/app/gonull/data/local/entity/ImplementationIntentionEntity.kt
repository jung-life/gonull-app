package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "implementation_intentions")
data class ImplementationIntentionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String? = null, // null = applies to all apps
    val thenAction: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
