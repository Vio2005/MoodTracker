package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.databinding.ActivityMoodDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoodDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodDetailBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private var dailyMoodId: Long = -1L

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
                            CalendarActivity.instance?.refreshCalendar()
                            finish()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Load mood + tags + note + icon
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

            // Show UI
            binding.tvDate.text = "Date: ${dailyMood.date}"
            binding.tvMood.text = "Mood: ${mood?.type ?: "Unknown"}"

            // Set mood icon
            val moodIconRes = when (mood?.type) {
                "Happy" -> R.drawable.jcir
                "Sad" -> R.drawable.scir
                "Angry" -> R.drawable.acir
                "Disgust" -> R.drawable.disguestmood
                "Lazy" -> R.drawable.lcir
                "Anxiety" -> R.drawable.anxietymood
                "Fear" -> R.drawable.fcir
                "Embarrass" -> R.drawable.embmood
                "Envy" -> R.drawable.envymood
                else -> R.drawable.mood_dot
            }
            binding.ivMoodIcon.setImageResource(moodIconRes)

            // Show tags as comma-separated string
            binding.tvTags.text = if (savedTags.isNotEmpty()) {
                "Tags: ${savedTags.joinToString { it.name }}"
            } else {
                "Tags: None"
            }

            binding.etNote.setText(dailyMood.note ?: "")

            // Refresh calendar
            CalendarActivity.instance?.refreshCalendar()
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
