package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.data.entity.MoodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MoodSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: AppDatabase
    private var selectedDate: String? = null
    private var currentUserId: Long = -1L

    private val REQUEST_TAGS = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_selection)

        recyclerView = findViewById(R.id.rvMoods)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 images per row

        db = AppDatabase.getInstance(this)

        // Get date from intent or default to today
        selectedDate = intent.getStringExtra("date") ?: getTodayDate()

        val sharedPref = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getLong("loggedInUserId", -1L)
        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadMoods()
    }

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    private fun loadMoods() {
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
                checkAndSaveMood(selectedMood)
            }
        }
    }

    private fun checkAndSaveMood(mood: MoodEntity) {
        if (selectedDate == null || currentUserId == -1L) return

        lifecycleScope.launch(Dispatchers.IO) {
            val existingMood =
                db.dailyMoodDao().getMoodByUserAndDate(currentUserId, selectedDate!!)
            val dailyMoodId = if (existingMood != null) {
                existingMood.dailyMoodId
            } else {
                db.dailyMoodDao().insertDailyMood(
                    DailyMoodEntity(
                        userId = currentUserId,
                        moodId = mood.moodId,
                        date = selectedDate!!
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MoodSelectionActivity,
                    "Mood saved: ${mood.type} on $selectedDate",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@MoodSelectionActivity, TagSelectionActivity::class.java)
                intent.putExtra("dailyMoodId", dailyMoodId)
                startActivityForResult(intent, REQUEST_TAGS)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAGS && resultCode == RESULT_OK) {
            val refresh = data?.getBooleanExtra("refreshCalendar", false) ?: false
            if (refresh) {
                val resultIntent = Intent()
                resultIntent.putExtra("refreshCalendar", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}