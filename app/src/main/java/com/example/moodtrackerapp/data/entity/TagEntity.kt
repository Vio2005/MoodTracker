package com.example.moodtrackerapp.data.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,
    val name: String,    // e.g. "Sports"
    val icon: String?=null    // optional emoji/icon
)
