package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.PartnerNotificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartnerNotificationLogDao {
    @Query("SELECT * FROM partner_notification_logs ORDER BY sentAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<PartnerNotificationLogEntity>>

    @Query("SELECT * FROM partner_notification_logs WHERE sentAt >= :since ORDER BY sentAt DESC")
    suspend fun getLogsSince(since: Long): List<PartnerNotificationLogEntity>

    @Query("SELECT COUNT(*) FROM partner_notification_logs WHERE notificationType = :type AND sentAt >= :since")
    suspend fun getNotificationCountOfTypeSince(type: String, since: Long): Int

    @Insert
    suspend fun insert(log: PartnerNotificationLogEntity)

    @Query("DELETE FROM partner_notification_logs WHERE sentAt < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM partner_notification_logs
            WHERE packageName = :packageName
            AND notificationType = :notificationType
            AND date(sentAt / 1000, 'unixepoch', 'localtime') = :date
        )
    """)
    suspend fun hasNotifiedToday(packageName: String, notificationType: String, date: String): Boolean
}
