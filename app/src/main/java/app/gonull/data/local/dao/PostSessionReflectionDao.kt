package app.gonull.data.local.dao

import androidx.room.*
import app.gonull.data.local.entity.PostSessionReflectionEntity

@Dao
interface PostSessionReflectionDao {
    @Insert
    suspend fun insertReflection(reflection: PostSessionReflectionEntity)

    @Query("SELECT AVG(worthItRating) FROM post_session_reflections")
    suspend fun getAverageWorthItRating(): Float?

    @Query("SELECT COUNT(*) FROM post_session_reflections WHERE worthItRating <= 2")
    suspend fun getNotWorthItCount(): Int

    @Query("SELECT COUNT(*) FROM post_session_reflections")
    suspend fun getTotalReflections(): Int
}
