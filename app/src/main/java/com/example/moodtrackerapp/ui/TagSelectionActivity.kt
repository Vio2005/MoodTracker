package com.example.moodtrackerapp.ui

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
    private lateinit var tagAdapter: MultiSelectTagAdapter

    private var moodId: Long = -1L
    private var dailyMoodId: Long = -1L
    private var userId: Long = -1L
    private var selectedDate: String? = null
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_selection)

        recyclerView = findViewById(R.id.rvTags)
        doneButton = findViewById(R.id.btnDone)
        etNote = findViewById(R.id.etNote)
        recyclerView.layoutManager = LinearLayoutManager(this)
        db = AppDatabase.getInstance(this)

        moodId = intent.getLongExtra("moodId", -1L)
        dailyMoodId = intent.getLongExtra("dailyMoodId", -1L)
        userId = intent.getLongExtra("userId", -1L)
        selectedDate = intent.getStringExtra("date") // <<< IMPORTANT FIX
        isEdit = intent.getBooleanExtra("isEdit", false)

        if (!isEdit && (moodId == -1L || userId == -1L || selectedDate.isNullOrEmpty())) {
            Toast.makeText(this, "Mood/User/Date not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTags()
        if (isEdit && dailyMoodId != -1L) loadExistingTagsAndNote()

        doneButton.setOnClickListener {
            val noteText = etNote.text.toString().takeIf { it.isNotBlank() }
            saveMoodTagsAndNote(noteText)
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

            tagAdapter = MultiSelectTagAdapter(tags) { _, _ -> /* listener handled internally */ }
            recyclerView.adapter = tagAdapter
        }
    }

    private fun loadExistingTagsAndNote() {
        lifecycleScope.launch {
            val dailyMood = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getDailyMoodById(dailyMoodId)
            } ?: return@launch

            val existingTags = withContext(Dispatchers.IO) {
                db.dailyMoodTagDao().getTagsByDailyMoodId(dailyMoodId)
                    .mapNotNull { db.tagDao().getTagById(it.tagId) }
            }

            etNote.setText(dailyMood.note)

            // Preselect existing tags in adapter
            tagAdapter.preselectTags(existingTags)
        }
    }

    private fun saveMoodTagsAndNote(note: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (isEdit && dailyMoodId != -1L) {
                val existingMood = db.dailyMoodDao().getDailyMoodById(dailyMoodId)
                if (existingMood != null) {
                    db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMoodId)

                    tagAdapter.getSelectedTags().distinctBy { it.tagId }.forEach { tag ->
                        db.dailyMoodTagDao().insertDailyMoodTag(
                            DailyMoodTagEntity(dailyMoodId = dailyMoodId, tagId = tag.tagId)
                        )
                    }

                    db.dailyMoodDao().updateDailyMood(
                        existingMood.copy(note = note, updatedAt = System.currentTimeMillis())
                    )
                }
            } else {
                // Use selectedDate instead of Date()!
                val newDailyMoodId = db.dailyMoodDao().insertDailyMood(
                    DailyMoodEntity(
                        userId = userId,
                        moodId = moodId,
                        date = selectedDate!!,
                        note = note
                    )
                )

                tagAdapter.getSelectedTags().distinctBy { it.tagId }.forEach { tag ->
                    db.dailyMoodTagDao().insertDailyMoodTag(
                        DailyMoodTagEntity(dailyMoodId = newDailyMoodId, tagId = tag.tagId)
                    )
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@TagSelectionActivity, "Saved successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
