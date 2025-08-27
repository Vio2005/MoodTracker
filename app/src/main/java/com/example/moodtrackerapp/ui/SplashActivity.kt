package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        val firstInstall = prefs.getBoolean("firstInstall", true)
        val currentUserId = prefs.getLong("loggedInUserId", -1L)

        when {
            firstInstall -> {
                // Mark firstInstall as false after first launch
                prefs.edit().putBoolean("firstInstall", false).apply()
                startActivity(Intent(this, IntroActivity::class.java))
            }
            currentUserId != -1L -> {
                // User logged in last time
                startActivity(Intent(this, MainActivity::class.java))
            }
            else -> {
                // User logged out last time
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        finish() // close splash
    }
}
