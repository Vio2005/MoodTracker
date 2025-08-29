package com.example.moodtrackerapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_moods",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MoodEntity::class,
            parentColumns = ["moodId"],
            childColumns = ["moodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),   // Speed up queries with userId
        Index(value = ["moodId"]),   // Speed up queries with moodId
        Index(value = ["date"])      // Add index for searching by date
    ]
)
data class DailyMoodEntity(
    @PrimaryKey(autoGenerate = true)
    val dailyMoodId: Long = 0,

    val userId: Long,      // FK → users.userId
    val moodId: Long,      // FK → moods.moodId

    val date: String,      // YYYY-MM-DD format

    val note: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
