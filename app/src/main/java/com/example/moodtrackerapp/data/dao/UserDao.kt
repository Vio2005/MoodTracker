package com.example.moodtrackerapp.data.dao

import androidx.room.*
import com.example.moodtrackerapp.data.entity.UserEntity

@Dao
interface UserDao {
    // Register new user
    @Insert(onConflict = OnConflictStrategy.ABORT) // prevents duplicate email
    suspend fun insertUser(user: UserEntity): Long

    // Login: check if user exists with given email + password
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): UserEntity?

    // Get user by ID (for session restore)
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    // Optional: check if email already exists
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
}
