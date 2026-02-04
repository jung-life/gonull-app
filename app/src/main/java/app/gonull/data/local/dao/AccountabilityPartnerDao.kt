package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.AccountabilityPartnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountabilityPartnerDao {
    @Query("SELECT * FROM accountability_partners WHERE id = 1")
    suspend fun getPartner(): AccountabilityPartnerEntity?

    @Query("SELECT * FROM accountability_partners WHERE id = 1")
    fun observePartner(): Flow<AccountabilityPartnerEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM accountability_partners WHERE id = 1 AND isActive = 1)")
    suspend fun hasActivePartner(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(partner: AccountabilityPartnerEntity)

    @Query("UPDATE accountability_partners SET isActive = :isActive WHERE id = 1")
    suspend fun setActive(isActive: Boolean)

    @Query("DELETE FROM accountability_partners WHERE id = 1")
    suspend fun delete()

    @Query("SELECT * FROM accountability_partners WHERE isActive = 1")
    suspend fun getActivePartners(): List<AccountabilityPartnerEntity>
}
