package com.example.moodtrackerapp.data.dao

import androidx.room.*
import com.example.moodtrackerapp.data.entity.DailyMoodTagEntity

@Dao
interface DailyMoodTagDao {

    // Insert a single DailyMoodTag
    @Insert
    suspend fun insertDailyMoodTag(dailyMoodTag: DailyMoodTagEntity)

    // Insert multiple DailyMoodTag entities at once
    @Insert
    suspend fun insertDailyMoodTags(dailyMoodTags: List<DailyMoodTagEntity>)

    // Delete a single DailyMoodTag entity
    @Delete
    suspend fun deleteDailyMoodTag(dailyMoodTag: DailyMoodTagEntity)

    // Delete all tags for a given dailyMoodId
    @Query("DELETE FROM daily_mood_tags WHERE dailyMoodId = :dailyMoodId")
    suspend fun deleteTagsByDailyMoodId(dailyMoodId: Long)

    // Get all tags for a specific dailyMoodId
    @Query("SELECT * FROM daily_mood_tags WHERE dailyMoodId = :dailyMoodId")
    suspend fun getTagsByDailyMoodId(dailyMoodId: Long): List<DailyMoodTagEntity>
}
