package com.example.moodtrackerapp.data

import com.example.moodtrackerapp.data.dao.UserDao
import com.example.moodtrackerapp.data.entity.UserEntity

class UserRepository(private val userDao: UserDao) {

    // Register new user (with email duplicate check)
    suspend fun registerUser(user: UserEntity): Result<Long> {
        val existingUser = userDao.getUserByEmail(user.email)
        return if (existingUser != null) {
            Result.failure(Exception("Email already exists"))
        } else {
            val userId = userDao.insertUser(user)
            Result.success(userId)
        }
    }

    // Login user
    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        val user = userDao.loginUser(email, password)
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    // Get user by ID (for session restore)
    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }
}