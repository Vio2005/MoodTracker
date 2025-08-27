package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moodtrackerapp.R

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if app has been opened before
        val sharedPref = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        val isFirstInstall = sharedPref.getBoolean("isFirstInstall", true)

        if (!isFirstInstall) {
            // Already opened before → go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_intro) // container for fragments
    }
}
