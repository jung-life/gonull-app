package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.QuestLogEntity

@Dao
interface QuestLogDao {
    @Insert
    suspend fun insertQuest(quest: QuestLogEntity)

    @Query("SELECT COUNT(*) FROM quest_log")
    suspend fun getTotalQuestsCompleted(): Int

    @Query("SELECT COUNT(*) FROM quest_log WHERE completedAt >= :since")
    suspend fun getQuestsCompletedSince(since: Long): Int

    @Query("SELECT * FROM quest_log ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentQuests(limit: Int = 10): List<QuestLogEntity>

    @Query("SELECT * FROM quest_log WHERE questCategory = :category ORDER BY completedAt DESC")
    suspend fun getQuestsByCategory(category: String): List<QuestLogEntity>

    @Query("SELECT questCategory, COUNT(*) as count FROM quest_log GROUP BY questCategory")
    suspend fun getQuestCountByCategory(): List<QuestCategoryCount>
}

data class QuestCategoryCount(
    val questCategory: String,
    val count: Int
)
