package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.ImplementationIntentionEntity

@Dao
interface ImplementationIntentionDao {
    @Insert
    suspend fun insertIntention(intention: ImplementationIntentionEntity)

    @Update
    suspend fun updateIntention(intention: ImplementationIntentionEntity)

    @Delete
    suspend fun deleteIntention(intention: ImplementationIntentionEntity)

    @Query("SELECT * FROM implementation_intentions WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllActiveIntentions(): List<ImplementationIntentionEntity>

    @Query("""
        SELECT * FROM implementation_intentions
        WHERE isActive = 1 AND (packageName = :packageName OR packageName IS NULL)
        ORDER BY packageName DESC
        LIMIT 1
    """)
    suspend fun getIntentionForApp(packageName: String): ImplementationIntentionEntity?
}
