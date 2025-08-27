package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.data.entity.MoodEntity
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

        val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("loggedInUserId", -1L)
        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvWelcome.text = "Welcome to Mood Tracker!"
        todayDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(Date())

        setupBottomNavigation()
        loadTodayMood()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadTodayMood() {
        lifecycleScope.launch {
            val dailyMood = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
            }

            if (dailyMood != null) {
                showMoodDetails(dailyMood)
            } else {
                showMoodSelection()
            }
        }
    }

    private suspend fun showMoodDetails(dailyMood: DailyMoodEntity) {
        val mood = withContext(Dispatchers.IO) {
            db.moodDao().getMoodById(dailyMood.moodId)
        }
        val tags = withContext(Dispatchers.IO) {
            db.tagDao().getTagsByDailyMoodId(dailyMood.dailyMoodId)
        }

        binding.layoutMoodDetails.visibility = View.VISIBLE
        binding.rvMoods.visibility = View.GONE
        binding.btnSelectTodayMood.visibility = View.GONE

        binding.ivTodayMoodIcon.setImageResource(mood?.icon ?: R.drawable.mood_dot)
        binding.tvTodayMoodType.text = mood?.type ?: ""
        binding.tvTodayTags.text = if (tags.isNotEmpty()) tags.joinToString { it.name } else "No Tags"
        binding.tvTodayNote.text = dailyMood.note ?: ""

        binding.btnEditMood.visibility = View.VISIBLE
        binding.btnEditTagsNote.visibility = View.VISIBLE
        binding.btnDeleteTodayMood.visibility = View.VISIBLE

        // Edit Mood
        binding.btnEditMood.setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_MOOD)
        }

        // Edit Tags & Note
        binding.btnEditTagsNote.setOnClickListener {
            val intent = Intent(this, EditTagAndNoteActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_TAGS_NOTE)
        }

        // Delete Mood
        binding.btnDeleteTodayMood.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.dailyMoodDao().deleteDailyMoodById(dailyMood.dailyMoodId)
                db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMood.dailyMoodId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Today's mood deleted", Toast.LENGTH_SHORT).show()
                    loadTodayMood()
                }
            }
        }
    }

    private fun showMoodSelection() {
        binding.layoutMoodDetails.visibility = View.GONE
        binding.rvMoods.visibility = View.VISIBLE
        binding.btnSelectTodayMood.visibility = View.VISIBLE

        binding.rvMoods.layoutManager = GridLayoutManager(this, 3)
        lifecycleScope.launch {
            val moods: List<MoodEntity> = withContext(Dispatchers.IO) {
                val existingMoods = db.moodDao().getAllMoods()
                if (existingMoods.isEmpty()) {
                    val defaultMoods = listOf(
                        MoodEntity(type = "Happy", icon = R.drawable.joymood),
                        MoodEntity(type = "Sad", icon = R.drawable.sadmood),
                        MoodEntity(type = "Angry", icon = R.drawable.angrymood),
                        MoodEntity(type = "Disgust", icon = R.drawable.disguestmood),
                        MoodEntity(type = "Lazy", icon = R.drawable.lazymood),
                        MoodEntity(type = "Anxiety", icon = R.drawable.anxietymood),
                        MoodEntity(type = "Fear", icon = R.drawable.fearmood),
                        MoodEntity(type = "Embarrass", icon = R.drawable.embmood),
                        MoodEntity(type = "Envy", icon = R.drawable.envymood)
                    )
                    db.moodDao().insertMoods(defaultMoods)
                    db.moodDao().getAllMoods()
                } else existingMoods
            }

            binding.rvMoods.adapter = MoodAdapter(moods) { selectedMood ->
                saveTodayMood(selectedMood)
            }
        }

        binding.btnSelectTodayMood.setOnClickListener {
            showMoodSelection()
        }
    }

    private fun saveTodayMood(mood: MoodEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val existingMood = db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
            val dailyMoodId = if (existingMood != null) {
                existingMood.dailyMoodId
            } else {
                db.dailyMoodDao().insertDailyMood(
                    DailyMoodEntity(
                        userId = currentUserId,
                        moodId = mood.moodId,
                        date = todayDate
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Mood saved: ${mood.type}", Toast.LENGTH_SHORT).show()
                loadTodayMood()
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
