package com.example.moodtrackerapp.ui

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

class EditMoodActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private var dailyMoodId: Long = -1L
    private lateinit var recyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mood)

        dailyMoodId = intent.getLongExtra("dailyMoodId", -1L)
        if (dailyMoodId == -1L) {
            Toast.makeText(this, "Invalid mood ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.rvEditMood)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 moods per row

        loadMoods()
    }

    private fun loadMoods() {
        lifecycleScope.launch {
            val moods: List<MoodEntity> = withContext(Dispatchers.IO) {
                db.moodDao().getAllMoods()
            }

            moodAdapter = MoodAdapter(moods) { selectedMood ->
                updateMood(selectedMood)
            }
            recyclerView.adapter = moodAdapter
        }
    }

    private fun updateMood(selectedMood: MoodEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Pass dailyMoodId as Long directly
            val dailyMood: DailyMoodEntity? = db.dailyMoodDao().getDailyMoodById(dailyMoodId)
            if (dailyMood != null) {
                db.dailyMoodDao().updateDailyMood(
                    dailyMood.copy(
                        moodId = selectedMood.moodId,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditMoodActivity, "Mood updated", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
