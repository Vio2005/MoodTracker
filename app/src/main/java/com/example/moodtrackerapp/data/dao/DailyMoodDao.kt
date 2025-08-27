package com.example.moodtrackerapp.data.dao

import androidx.room.*
import com.example.moodtrackerapp.data.entity.DailyMoodEntity

@Dao
interface DailyMoodDao {
    @Insert
    suspend fun insertDailyMood(dailyMood: DailyMoodEntity): Long

    @Update
    suspend fun updateDailyMood(dailyMood: DailyMoodEntity)

    @Query("SELECT * FROM daily_moods WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyMoodForDate(userId: Long, date: String): DailyMoodEntity?

    @Query("SELECT * FROM daily_moods WHERE userId = :userId")
    suspend fun getAllDailyMoodsForUser(userId: Long): List<DailyMoodEntity>

    @Delete
    suspend fun deleteDailyMood(dailyMood: DailyMoodEntity)

    @Query("SELECT * FROM daily_moods WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getMoodByUserAndDate(userId: Long, date: String): DailyMoodEntity?

    @Query("SELECT * FROM daily_moods WHERE userId = :userId")
    suspend fun getAllByUser(userId: Long): List<DailyMoodEntity>

    @Query("SELECT * FROM daily_moods WHERE dailyMoodId = :id LIMIT 1")
    suspend fun getDailyMoodById(id: Long): DailyMoodEntity?

    @Query("DELETE FROM daily_moods WHERE dailyMoodId = :id")
    suspend fun deleteDailyMoodById(id: Long)

    @Query("SELECT * FROM daily_moods WHERE userId = :userId")
    suspend fun getAllMoodsByUser(userId: Long): List<DailyMoodEntity>





    @Query("SELECT * FROM daily_moods WHERE dailyMoodId = :id LIMIT 1")
    suspend fun getDailyMoodById(id: Int): DailyMoodEntity





}
