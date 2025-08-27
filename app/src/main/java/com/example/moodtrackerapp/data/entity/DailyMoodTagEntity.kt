package com.example.moodtrackerapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_mood_tags",
    foreignKeys = [
        ForeignKey(
            entity = DailyMoodEntity::class,
            parentColumns = ["dailyMoodId"],
            childColumns = ["dailyMoodId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dailyMoodId"]),  // speeds up joins on dailyMoodId
        Index(value = ["tagId"])         // speeds up joins on tagId
    ]
)
data class DailyMoodTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dailyMoodId: Long,  // FK → daily_moods.dailyMoodId
    val tagId: Long         // FK → tags.tagId
)
