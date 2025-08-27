package com.example.moodtrackerapp.data.dao

import androidx.room.*
import com.example.moodtrackerapp.data.entity.TagEntity

@Dao
interface TagDao {
    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE tagId = :id LIMIT 1")
    suspend fun getTagById(id: Long): TagEntity

    @Insert
    suspend fun insertTags(tag: List<TagEntity>)

    @Query("""
    SELECT t.*
    FROM tags t
    INNER JOIN daily_mood_tags dmt ON t.tagId = dmt.tagId
    WHERE dmt.dailyMoodId = :dailyMoodId
""")
    suspend fun getTagsByDailyMoodId(dailyMoodId: Long): List<TagEntity>





}
