package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodTagEntity
import com.example.moodtrackerapp.data.entity.TagEntity
import com.example.moodtrackerapp.databinding.ActivityMoodDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoodDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodDetailBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var tagAdapter: TagAdapter
    private var dailyMoodId: Long = -1L
    private var selectedTags = mutableSetOf<TagEntity>()

    private val REQUEST_EDIT = 3001
    private val REQUEST_EDIT_MOOD = 3002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dailyMoodId = intent.getLongExtra("dailyMoodId", -1L)
        if (dailyMoodId == -1L) {
            Toast.makeText(this, "Invalid mood ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        binding.rvTags.layoutManager = LinearLayoutManager(this)
        tagAdapter = TagAdapter { tag, _ -> }
        binding.rvTags.adapter = tagAdapter

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Edit Tag & Note button
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditTagAndNoteActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT)
        }

        // Edit Mood button
        binding.btneditmood.setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_MOOD)
        }

        // Delete button with confirmation
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Mood")
                .setMessage("Do you really want to delete this mood?")
                .setPositiveButton("Yes") { dialog, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.dailyMoodDao().deleteDailyMoodById(dailyMoodId)
                        db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMoodId)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MoodDetailActivity, "Mood deleted", Toast.LENGTH_SHORT).show()
                            CalendarActivity.instance?.refreshCalendar() // refresh calendar immediately
                            finish()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Load mood + tags + note
        loadMoodDetail()
    }

    private fun loadMoodDetail() {
        lifecycleScope.launch {
            val dailyMood = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getDailyMoodById(dailyMoodId.toInt())
            }

            if (dailyMood == null) {
                Toast.makeText(this@MoodDetailActivity, "Mood not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val mood = withContext(Dispatchers.IO) {
                db.moodDao().getMoodById(dailyMood.moodId)
            }

            val savedTags = withContext(Dispatchers.IO) {
                db.tagDao().getTagsByDailyMoodId(dailyMoodId)
            }

            selectedTags.clear()
            selectedTags.addAll(savedTags)

            // Show UI
            binding.tvDate.text = "Date: ${dailyMood.date}"
            binding.tvMood.text = "Mood: ${mood?.type ?: "Unknown"}"
            binding.etNote.setText(dailyMood.note ?: "")
            tagAdapter.submitList(savedTags, savedTags)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_EDIT -> {
                if (resultCode == RESULT_OK) {
                    loadMoodDetail()
                    Toast.makeText(this, "Tags & note updated", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_EDIT_MOOD -> {
                if (resultCode == RESULT_OK) {
                    loadMoodDetail()
                    Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
