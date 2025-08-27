package com.example.moodtrackerapp.data.dao

import androidx.room.*
import com.example.moodtrackerapp.data.entity.MoodEntity

@Dao
interface MoodDao {
    @Insert
    suspend fun insertMood(mood: MoodEntity): Long

    @Query("SELECT * FROM moods")
    suspend fun getAllMoods(): List<MoodEntity>

    @Query("SELECT * FROM moods WHERE moodId = :id LIMIT 1")
    suspend fun getMoodById(id: Long): MoodEntity?

    @Insert
    suspend fun insertMoods(moods: List<MoodEntity>)


}
