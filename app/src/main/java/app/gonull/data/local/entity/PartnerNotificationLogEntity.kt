package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partner_notification_logs")
data class PartnerNotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partnerPhone: String,
    val notificationType: String, // EMERGENCY_BYPASS, MULTIPLE_BYPASSES, BUDGET_EXCEEDED
    val packageName: String?,
    val message: String,
    val sentAt: Long = System.currentTimeMillis(),
    val wasSuccessful: Boolean = true
)
