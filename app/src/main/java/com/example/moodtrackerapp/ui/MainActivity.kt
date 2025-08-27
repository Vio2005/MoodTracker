package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db by lazy { AppDatabase.getInstance(this) }

    private var currentUserId: Long = -1L
    private var todayDate: String = ""

    private val REQUEST_MOOD_SELECTION = 1001
    private val REQUEST_EDIT_MOOD = 1002
    private val REQUEST_EDIT_TAGS_NOTE = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check first install or logged-in user
        val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        val firstInstall = prefs.getBoolean("firstInstall", true)
        currentUserId = prefs.getLong("loggedInUserId", -1L)

        if (firstInstall) {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
            return
        }

        if (currentUserId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.tvWelcome.text = "Welcome to Mood Tracker!"
        todayDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(Date())

        loadTodayMood()
        setupButtons()
        setupBottomNavigation() // 👈 added
    }

    private fun setupButtons() {
        // Calendar button (optional if using bottom nav too)
        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
            prefs.edit().putLong("loggedInUserId", -1L).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Select today's mood
        binding.btnSelectTodayMood.setOnClickListener {
            val intent = Intent(this, MoodSelectionActivity::class.java)
            intent.putExtra("date", todayDate)
            startActivityForResult(intent, REQUEST_MOOD_SELECTION)
        }

        // Edit today's mood
        binding.btnEditMood.setOnClickListener {
            lifecycleScope.launch {
                val dailyMood = getTodayMood()
                if (dailyMood != null) {
                    val intent = Intent(this@MainActivity, EditMoodActivity::class.java)
                    intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
                    startActivityForResult(intent, REQUEST_EDIT_MOOD)
                }
            }
        }

        // Edit tags & note
        binding.btnEditTagsNote.setOnClickListener {
            lifecycleScope.launch {
                val dailyMood = getTodayMood()
                if (dailyMood != null) {
                    val intent = Intent(this@MainActivity, EditTagAndNoteActivity::class.java)
                    intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
                    startActivityForResult(intent, REQUEST_EDIT_TAGS_NOTE)
                }
            }
        }

        // Delete today's mood
        binding.btnDeleteTodayMood.setOnClickListener {
            lifecycleScope.launch {
                val dailyMood = getTodayMood()
                if (dailyMood != null) {
                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Delete Mood")
                            .setMessage("Do you want to delete today's mood?")
                            .setPositiveButton("Yes") { _, _ ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    db.dailyMoodDao().deleteDailyMoodById(dailyMood.dailyMoodId)
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Today's mood deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadTodayMood()
                                    }
                                }
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }
            }
        }
    }

    // 👇 NEW: Bottom Navigation setup
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in MainActivity
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // example
                    true
                }
                else -> false
            }
        }
    }

    private suspend fun getTodayMood(): DailyMoodEntity? = withContext(Dispatchers.IO) {
        db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
    }

    private fun loadTodayMood() {
        lifecycleScope.launch {
            val dailyMood = getTodayMood()
            if (dailyMood != null) {
                val mood = withContext(Dispatchers.IO) {
                    db.moodDao().getMoodById(dailyMood.moodId)
                }
                val tags = withContext(Dispatchers.IO) {
                    db.tagDao().getTagsByDailyMoodId(dailyMood.dailyMoodId)
                }

                binding.ivTodayMoodIcon.setImageResource(mood?.icon ?: R.drawable.mood_dot)
                binding.tvTodayMoodType.text = mood?.type ?: ""
                binding.tvTodayTags.text = if (tags.isNotEmpty()) tags.joinToString { it.name } else ""
                binding.tvTodayNote.text = dailyMood.note ?: ""

                binding.ivTodayMoodIcon.visibility = android.view.View.VISIBLE
                binding.tvTodayMoodType.visibility = android.view.View.VISIBLE
                binding.tvTodayTags.visibility = android.view.View.VISIBLE
                binding.tvTodayNote.visibility = android.view.View.VISIBLE

                binding.btnEditMood.visibility = android.view.View.VISIBLE
                binding.btnEditTagsNote.visibility = android.view.View.VISIBLE
                binding.btnDeleteTodayMood.visibility = android.view.View.VISIBLE
                binding.btnSelectTodayMood.visibility = android.view.View.GONE
            } else {
                binding.ivTodayMoodIcon.visibility = android.view.View.GONE
                binding.tvTodayMoodType.visibility = android.view.View.GONE
                binding.tvTodayTags.visibility = android.view.View.GONE
                binding.tvTodayNote.visibility = android.view.View.GONE

                binding.btnEditMood.visibility = android.view.View.GONE
                binding.btnEditTagsNote.visibility = android.view.View.GONE
                binding.btnDeleteTodayMood.visibility = android.view.View.GONE
                binding.btnSelectTodayMood.visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadTodayMood()
        }
    }
}
