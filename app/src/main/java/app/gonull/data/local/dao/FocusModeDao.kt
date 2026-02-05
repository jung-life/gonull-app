package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.FocusModeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusModeDao {
    @Query("SELECT * FROM focus_modes")
    fun getAllModes(): Flow<List<FocusModeEntity>>

    @Query("SELECT * FROM focus_modes WHERE modeType = :modeType")
    suspend fun getMode(modeType: String): FocusModeEntity?

    @Query("SELECT * FROM focus_modes WHERE isActive = 1")
    suspend fun getActiveModes(): List<FocusModeEntity>

    @Query("SELECT * FROM focus_modes WHERE isActive = 1")
    fun getActiveModesFlow(): Flow<List<FocusModeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: FocusModeEntity)

    @Update
    suspend fun updateMode(mode: FocusModeEntity)

    @Query("UPDATE focus_modes SET isActive = :isActive, activatedAt = :activatedAt, expiresAt = :expiresAt WHERE modeType = :modeType")
    suspend fun setModeActive(modeType: String, isActive: Boolean, activatedAt: Long?, expiresAt: Long?)

    @Query("UPDATE focus_modes SET allowedPackages = :packages WHERE modeType = :modeType")
    suspend fun updateAllowedPackages(modeType: String, packages: String)

    @Query("SELECT allowedPackages FROM focus_modes WHERE isActive = 1")
    suspend fun getAllActiveAllowedPackages(): List<String>

    // Check if a specific package is allowed by any active focus mode
    @Query("""
        SELECT COUNT(*) > 0 FROM focus_modes
        WHERE isActive = 1
        AND (expiresAt IS NULL OR expiresAt > :currentTime)
        AND allowedPackages LIKE '%' || :packageName || '%'
    """)
    suspend fun isPackageAllowedByActiveFocusMode(packageName: String, currentTime: Long): Boolean

    // Deactivate expired modes
    @Query("UPDATE focus_modes SET isActive = 0, activatedAt = NULL, expiresAt = NULL WHERE isActive = 1 AND expiresAt IS NOT NULL AND expiresAt <= :currentTime")
    suspend fun deactivateExpiredModes(currentTime: Long)
}
