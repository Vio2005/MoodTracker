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
    private var hasChanges = false

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

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditTagAndNoteActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT)
        }

        binding.btneditmood.setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            intent.putExtra("dailyMoodId", dailyMoodId)
            startActivityForResult(intent, REQUEST_EDIT_MOOD)
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Mood")
                .setMessage("Do you really want to delete this mood?")
                .setPositiveButton("Yes") { dialog, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.dailyMoodDao().deleteDailyMoodById(dailyMoodId)
                        db.dailyMoodTagDao().deleteTagsByDailyMoodId(dailyMoodId)
                        hasChanges = true
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MoodDetailActivity, "Mood deleted", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        loadMoodDetail()
    }

    private fun loadMoodDetail() {
        lifecycleScope.launch {
            val dailyMood = withContext(Dispatchers.IO) {
                db.dailyMoodDao().getDailyMoodById(dailyMoodId) // <-- Pass Long directly
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

            binding.tvDate.text = "Date: ${dailyMood.date}"
            binding.tvMood.text = "Mood: ${mood?.type ?: "Unknown"}"

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

            binding.tvTags.text = if (savedTags.isNotEmpty()) {
                "Tags: ${savedTags.joinToString { it.name }}"
            } else {
                "Tags: None"
            }

            binding.etNote.setText(dailyMood.note ?: "")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            hasChanges = true
            loadMoodDetail()
        }
    }

    override fun onBackPressed() {
        if (hasChanges) {
            setResult(RESULT_OK)
        }
        super.onBackPressed()
    }
}
