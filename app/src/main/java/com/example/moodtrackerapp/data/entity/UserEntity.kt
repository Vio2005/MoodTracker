package com.example.moodtrackerapp.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey (autoGenerate = true)
    val userId: Long=0,
    val email: String,
    val password: String,
    var username: String,
    val createdAt: Long = System.currentTimeMillis()
)
