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

    private val REQUEST_MOOD_SELECTION = 1001
    private val REQUEST_EDIT_MOOD = 1002
    private val REQUEST_EDIT_TAGS_NOTE = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvMoods)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columns

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
                R.id.nav_home -> {
                    if (this !is MainActivity) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_calendar -> {
                    if (this !is CalendarActivity) {
                        val intent = Intent(this, CalendarActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadTodayMood() {
        lifecycleScope.launch {
            val dailyMood = getTodayMood()
            if (dailyMood != null) {
                val mood = withContext(Dispatchers.IO) { db.moodDao().getMoodById(dailyMood.moodId) }
                val tags = withContext(Dispatchers.IO) { db.tagDao().getTagsByDailyMoodId(dailyMood.dailyMoodId) }

                findViewById<View>(R.id.layoutMoodDetails).visibility = View.VISIBLE
                findViewById<View>(R.id.rvMoods).visibility = View.GONE

                findViewById<android.widget.ImageView>(R.id.ivTodayMoodIcon).setImageResource(mood?.icon ?: R.drawable.mood_dot)
                findViewById<android.widget.TextView>(R.id.tvTodayMoodType).text = mood?.type ?: ""
                findViewById<android.widget.TextView>(R.id.tvTodayTags).text = if (tags.isNotEmpty()) tags.joinToString { it.name } else "No Tags"
                findViewById<android.widget.TextView>(R.id.tvTodayNote).text = dailyMood.note ?: ""

                findViewById<View>(R.id.btnEditMood).visibility = View.VISIBLE
                findViewById<View>(R.id.btnEditTagsNote).visibility = View.VISIBLE
                findViewById<View>(R.id.btnDeleteTodayMood).visibility = View.VISIBLE
                findViewById<View>(R.id.btnSelectTodayMood).visibility = View.GONE

                setupDetailButtons(dailyMood)
            } else {
                findViewById<View>(R.id.layoutMoodDetails).visibility = View.GONE
                findViewById<View>(R.id.rvMoods).visibility = View.VISIBLE

                loadMoodsForSelection()
            }
        }
    }

    private fun setupDetailButtons(dailyMood: DailyMoodEntity) {
        findViewById<View>(R.id.btnEditMood).setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_MOOD)
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.btnEditTagsNote).setOnClickListener {
            val intent = Intent(this, EditTagAndNoteActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMood.dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_TAGS_NOTE)
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.btnDeleteTodayMood).setOnClickListener {
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

    private suspend fun getTodayMood(): DailyMoodEntity? = withContext(Dispatchers.IO) {
        db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
    }

    private fun loadMoodsForSelection() {
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
                saveMood(selectedMood)
            }
        }
    }

    private fun saveMood(mood: MoodEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val existingMood = db.dailyMoodDao().getMoodByUserAndDate(currentUserId, todayDate)
            val dailyMoodId = if (existingMood != null) {
                existingMood.dailyMoodId
            } else {
                db.dailyMoodDao().insertDailyMood(
                    DailyMoodEntity(userId = currentUserId, moodId = mood.moodId, date = todayDate)
                )
            }
            withContext(Dispatchers.Main) {
                val intent = Intent(this@MainActivity, TagSelectionActivity::class.java)
                intent.putExtra("dailyMoodId", dailyMoodId)
                startActivityForResult(intent, REQUEST_EDIT_TAGS_NOTE)
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadTodayMood()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0) // Disable exit animation
    }
}
