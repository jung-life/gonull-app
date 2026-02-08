package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val entryText: String,
    val linkedFocusMode: String? = null,
    val focusDurationMinutes: Int? = null,
    val mood: String? = null
)
