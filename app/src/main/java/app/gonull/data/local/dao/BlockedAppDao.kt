package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps WHERE isActive = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isActive = 1")
    suspend fun getActiveBlockedAppsSync(): List<BlockedAppEntity>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName AND isActive = 1)")
    suspend fun isAppBlocked(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedAppByPackage(packageName: String)

    @Query("UPDATE blocked_apps SET isActive = :isActive WHERE packageName = :packageName")
    suspend fun setAppActive(packageName: String, isActive: Boolean)

    @Query("UPDATE blocked_apps SET budgetMinutes = :budgetMinutes, budgetEnabled = :budgetEnabled WHERE packageName = :packageName")
    suspend fun updateBudget(packageName: String, budgetMinutes: Int?, budgetEnabled: Boolean)

    @Query("SELECT * FROM blocked_apps WHERE budgetEnabled = 1")
    suspend fun getAppsWithBudget(): List<BlockedAppEntity>

    @Query("SELECT budgetMinutes FROM blocked_apps WHERE packageName = :packageName AND budgetEnabled = 1")
    suspend fun getBudgetMinutes(packageName: String): Int?
}
