package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_partners")
data class AccountabilityPartnerEntity(
    @PrimaryKey
    val id: Int = 1, // Single partner for simplicity
    val name: String,
    val phoneNumber: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
