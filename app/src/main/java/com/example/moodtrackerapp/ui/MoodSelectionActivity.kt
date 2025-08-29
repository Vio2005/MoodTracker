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
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        db = AppDatabase.getInstance(this)

        // Use date passed from CalendarActivity or fallback to today
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

            recyclerView.adapter = MoodAdapter(moods) { mood ->
                // Delete old mood and tags for this date before going to tag selection
                lifecycleScope.launch(Dispatchers.IO) {
                    val oldMood = db.dailyMoodDao().getMoodByUserAndDate(currentUserId, selectedDate!!)
                    oldMood?.let {
                        db.dailyMoodTagDao().deleteTagsByDailyMoodId(it.dailyMoodId)
                        db.dailyMoodDao().deleteDailyMoodById(it.dailyMoodId)
                    }

                    // Open TagSelectionActivity with new mood
                    val intent = Intent(this@MoodSelectionActivity, TagSelectionActivity::class.java)
                    intent.putExtra("moodId", mood.moodId)
                    intent.putExtra("date", selectedDate) // pass correct selected date
                    intent.putExtra("userId", currentUserId)
                    withContext(Dispatchers.Main) {
                        startActivityForResult(intent, REQUEST_TAGS)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAGS && resultCode == RESULT_OK) {
            setResult(RESULT_OK) // notify CalendarActivity to refresh
            finish()
        }
    }
}
