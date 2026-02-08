package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Insert
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntriesFlow(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int = 5): List<JournalEntryEntity>

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getTotalEntries(): Int

    @Query("SELECT * FROM journal_entries WHERE linkedFocusMode = :mode ORDER BY timestamp DESC")
    suspend fun getEntriesByMode(mode: String): List<JournalEntryEntity>
}
