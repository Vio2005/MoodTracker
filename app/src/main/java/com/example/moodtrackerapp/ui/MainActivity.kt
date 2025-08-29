package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.data.entity.MoodEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db by lazy { AppDatabase.getInstance(this) }
    private var currentUserId: Long = -1L
    private var todayDate: String = ""
    private val REQUEST_TAGS_NOTE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvMoods)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("loggedInUserId", -1L)
        todayDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(Date())

        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(0, 0)
            return
        }

        setupBottomNavigation()
        loadTodayMood()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, MoreActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadTodayMood() {
        lifecycleScope.launch {
            val dailyMood: DailyMoodEntity? = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
            }

            if (dailyMood != null) {
                displaySavedMood(dailyMood)
            } else {
                showMoodSelection()
            }
        }
    }

    private fun displaySavedMood(dailyMood: DailyMoodEntity) {
        lifecycleScope.launch {
            val mood = withContext(Dispatchers.IO) { db.moodDao().getMoodById(dailyMood.moodId) }
            val tags = withContext(Dispatchers.IO) { db.tagDao().getTagsByDailyMoodId(dailyMood.dailyMoodId) }

            findViewById<View>(R.id.layoutMoodDetails).visibility = View.VISIBLE
            findViewById<View>(R.id.rvMoods).visibility = View.GONE

            findViewById<android.widget.ImageView>(R.id.ivTodayMoodIcon).setImageResource(mood?.icon ?: R.drawable.mood_dot)
            findViewById<android.widget.TextView>(R.id.tvTodayMoodType).text = mood?.type ?: ""
            findViewById<android.widget.TextView>(R.id.tvTodayTags).text =
                if (tags.isNotEmpty()) tags.distinctBy { it.tagId }.joinToString { it.name } else "No Tags"
            findViewById<android.widget.TextView>(R.id.tvTodayNote).text = dailyMood.note ?: ""

            findViewById<View>(R.id.btnEditMood).visibility = View.VISIBLE
            findViewById<View>(R.id.btnEditTagsNote).visibility = View.VISIBLE
            findViewById<View>(R.id.btnDeleteTodayMood).visibility = View.VISIBLE
            findViewById<View>(R.id.btnSelectTodayMood).visibility = View.GONE

            setupDetailButtons(dailyMood)
        }
    }

    private fun showMoodSelection() {
        findViewById<View>(R.id.layoutMoodDetails).visibility = View.GONE
        findViewById<View>(R.id.rvMoods).visibility = View.VISIBLE

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

            recyclerView.adapter = MoodAdapter(moods) { selectedMood ->
                val intent = Intent(this@MainActivity, TagSelectionActivity::class.java)
                intent.putExtra("moodId", selectedMood.moodId.toLong())
                intent.putExtra("date", todayDate)
                intent.putExtra("userId", currentUserId.toLong())
                intent.putExtra("isEdit", false)
                startActivityForResult(intent, REQUEST_TAGS_NOTE)
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun setupDetailButtons(dailyMood: DailyMoodEntity) {
        // ✅ Edit mood (now sends dailyMoodId + moodId)
        findViewById<View>(R.id.btnEditMood).setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            intent.putExtra("date", dailyMood.date)
            intent.putExtra("userId", dailyMood.userId)
            intent.putExtra("moodId", dailyMood.moodId)
            intent.putExtra("isEdit", true)
            startActivityForResult(intent, REQUEST_TAGS_NOTE)
            overridePendingTransition(0, 0)
        }

        // Edit tags/note only
        findViewById<View>(R.id.btnEditTagsNote).setOnClickListener {
            val intent = Intent(this, TagSelectionActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            intent.putExtra("userId", dailyMood.userId)
            intent.putExtra("moodId", dailyMood.moodId)
            intent.putExtra("date", dailyMood.date)
            intent.putExtra("isEdit", true)
            startActivityForResult(intent, REQUEST_TAGS_NOTE)
            overridePendingTransition(0, 0)
        }

        // Delete today's mood
        findViewById<View>(R.id.btnDeleteTodayMood).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMood.dailyMoodId)
                db.dailyMoodDao().deleteDailyMoodById(dailyMood.dailyMoodId)
                withContext(Dispatchers.Main) { loadTodayMood() }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) loadTodayMood()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}