package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.moodtrackerapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.moodtrackerapp.data.AppDatabase
import androidx.activity.result.contract.ActivityResultContracts

class MoreActivity : AppCompatActivity() {

    private lateinit var cardAbout: CardView
    private lateinit var cardSettings: CardView
    private lateinit var cardEditMoods: CardView
    private lateinit var cardProfile: CardView
    private lateinit var cardLogout: CardView
    private lateinit var tvVersionTitle: TextView
    private lateinit var ivEditUsername: ImageView

    private val db by lazy { AppDatabase.getInstance(this) }
    private var currentUserId: Long = -1L

    // Launcher for EditUsernameActivity
    private val editUsernameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newUsername = result.data?.getStringExtra("newUsername")
            if (!newUsername.isNullOrEmpty()) {
                tvVersionTitle.text = newUsername
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        // Find your cards
        cardAbout = findViewById(R.id.cardAbout)

        cardEditMoods = findViewById(R.id.cardEditMoods)
        cardProfile = findViewById(R.id.cardProfile)
        cardLogout = findViewById(R.id.cardLogout)

        tvVersionTitle = findViewById(R.id.version_title)
        ivEditUsername = findViewById(R.id.version_edit_icon)

        // Get current logged-in user ID
        val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("loggedInUserId", -1L)

        if (currentUserId != -1L) {
            // Load username from database
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) { db.userDao().getUserById(currentUserId) }
                user?.let { tvVersionTitle.text = it.username }
            }
        }

        // Edit icon click
        ivEditUsername.setOnClickListener {
            val intent = Intent(this, EditUsernameActivity::class.java)
            intent.putExtra("userId", currentUserId)
            editUsernameLauncher.launch(intent)
        }

        // Click listeners for other cards
        cardAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }

        cardEditMoods.setOnClickListener { /* TODO: Open Reminder/Edit Moods */ }
        cardProfile.setOnClickListener { /* TODO: Open Profile/Location */ }

        // Logout card click listener
        cardLogout.setOnClickListener {
            showLogoutDialog()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile // highlight profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    // Logout confirmation dialog
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Clear user session
                val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
                with(prefs.edit()) {
                    clear()
                    apply()
                }
                // Navigate to LoginActivity and clear back stack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
