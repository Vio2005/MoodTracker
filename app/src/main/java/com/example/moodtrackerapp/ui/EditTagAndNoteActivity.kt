package com.example.moodtrackerapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodTagEntity
import com.example.moodtrackerapp.data.entity.TagEntity
import com.example.moodtrackerapp.databinding.ActivityEditTagAndNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditTagAndNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTagAndNoteBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var tagAdapter: TagAdapter
    private var dailyMoodId: Long = -1L
    private var selectedTags = mutableSetOf<TagEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTagAndNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dailyMoodId = intent.getLongExtra("dailyMoodId", -1L)
        if (dailyMoodId == -1L) {
            Toast.makeText(this, "Invalid mood ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // RecyclerView setup
        binding.rvTags.layoutManager = LinearLayoutManager(this)
        tagAdapter = TagAdapter { tag, isChecked ->
            if (isChecked) selectedTags.add(tag) else selectedTags.remove(tag)
        }
        binding.rvTags.adapter = tagAdapter

        // Load mood details
        loadData()

        // Back button
        binding.btnCancel.setOnClickListener { finish() }

        // Save button
        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val dailyMood = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getDailyMoodById(dailyMoodId.toInt())
            }

            if (dailyMood == null) {
                Toast.makeText(this@EditTagAndNoteActivity, "Mood not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val allTags = withContext(Dispatchers.IO) { db.tagDao().getAllTags() }
            val savedTags = withContext(Dispatchers.IO) {
                db.tagDao().getTagsByDailyMoodId(dailyMoodId)
            }

            selectedTags.clear()
            selectedTags.addAll(savedTags)

            binding.etNote.setText(dailyMood.note ?: "")
            tagAdapter.submitList(allTags, savedTags)
        }
    }

    private fun saveChanges() {
        val newNote = binding.etNote.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            val dailyMood = db.dailyMoodDao().getDailyMoodById(dailyMoodId.toInt())
            if (dailyMood != null) {
                // Update note
                db.dailyMoodDao().updateDailyMood(
                    dailyMood.copy(
                        note = newNote,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // Update tags
                db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMoodId)
                selectedTags.forEach { tag ->
                    db.dailyMoodTagDao().insertDailyMoodTag(
                        DailyMoodTagEntity(dailyMoodId = dailyMoodId, tagId = tag.tagId)
                    )
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditTagAndNoteActivity, "Changes saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
