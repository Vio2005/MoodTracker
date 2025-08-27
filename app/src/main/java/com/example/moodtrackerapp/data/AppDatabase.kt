package com.example.moodtrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodtrackerapp.data.dao.*
import com.example.moodtrackerapp.data.entity.*

@Database(
    entities = [
        UserEntity::class,
        MoodEntity::class,
        DailyMoodEntity::class,
        TagEntity::class,
        DailyMoodTagEntity::class
    ],
    version = 17,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun moodDao(): MoodDao
    abstract fun tagDao(): TagDao
    abstract fun dailyMoodDao(): DailyMoodDao
    abstract fun dailyMoodTagDao(): DailyMoodTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
