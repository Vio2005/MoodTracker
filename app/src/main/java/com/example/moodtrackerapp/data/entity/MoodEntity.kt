package com.example.moodtrackerapp.data.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true)
    val moodId: Long = 0,
    val type: String,   // e.g. "Happy"
    val icon: Int? = null,  // optional emoji/icon
    val color: String? = null  // optional color hex code
)
