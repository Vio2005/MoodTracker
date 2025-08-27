package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.data.entity.DailyMoodTagEntity
import com.example.moodtrackerapp.data.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doneButton: Button
    private lateinit var etNote: EditText
    private lateinit var db: AppDatabase
    private var dailyMoodId: Long = -1L
    private val selectedTags = mutableListOf<TagEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_selection)

        recyclerView = findViewById(R.id.rvTags)
        doneButton = findViewById(R.id.btnDone)
        etNote = findViewById(R.id.etNote) // EditText for optional note
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getInstance(this)

        dailyMoodId = intent.getLongExtra("dailyMoodId", -1L)
        if (dailyMoodId == -1L) {
            Toast.makeText(this, "Mood not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTags()

        doneButton.setOnClickListener {
            val noteText = etNote.text.toString().takeIf { it.isNotBlank() }
            saveSelectedTags(noteText)
        }
    }

    private fun loadTags() {
        lifecycleScope.launch {
            val tags: List<TagEntity> = withContext(Dispatchers.IO) {
                val existingTags = db.tagDao().getAllTags()
                if (existingTags.isEmpty()) {
                    val defaultTags = listOf(
                        TagEntity(name = "Sports"),
                        TagEntity(name = "Family"),
                        TagEntity(name = "Friends"),
                        TagEntity(name = "Work"),
                        TagEntity(name = "Travel")
                    )
                    db.tagDao().insertTags(defaultTags)
                    db.tagDao().getAllTags()
                } else existingTags
            }

            recyclerView.adapter = MultiSelectTagAdapter(tags) { tag, isSelected ->
                if (isSelected) selectedTags.add(tag)
                else selectedTags.remove(tag)
            }
        }
    }

    private fun saveSelectedTags(note: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Clear previous tags for this dailyMood
            db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMoodId)

            // Insert selected tags
            selectedTags.forEach { tag ->
                db.dailyMoodTagDao().insertDailyMoodTag(
                    DailyMoodTagEntity(
                        dailyMoodId = dailyMoodId,
                        tagId = tag.tagId
                    )
                )
            }

            // Update DailyMoodEntity with note
            val dailyMood = db.dailyMoodDao().getDailyMoodById(dailyMoodId)
            if (dailyMood != null) {
                db.dailyMoodDao().updateDailyMood(
                    dailyMood.copy(
                        note = note,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@TagSelectionActivity, "Tags & Note saved", Toast.LENGTH_SHORT).show()
                // Notify CalendarActivity to refresh
                val resultIntent = Intent().apply {
                    putExtra("refreshCalendar", true)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
