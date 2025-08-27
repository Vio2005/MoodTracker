package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.databinding.ActivityCalendarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.jvm.java

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private var currentUserId: Long = -1L
    private val REQUEST_MOOD_TAG = 1001

    companion object {
        var instance: CalendarActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getLong("loggedInUserId", -1L)
        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnGoMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    // Already in calendar, do nothing
                    true
                }
                R.id.nav_profile -> {

                    true
                }
                else -> false
            }
        }


        loadMoodDates()

        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val calendar = eventDay.calendar
                val selectedDate = "${calendar.get(Calendar.YEAR)}-" +
                        "${calendar.get(Calendar.MONTH) + 1}-" +
                        "${calendar.get(Calendar.DAY_OF_MONTH)}"

                CoroutineScope(Dispatchers.IO).launch {
                    val mood: DailyMoodEntity? =
                        db.dailyMoodDao().getMoodByUserAndDate(currentUserId, selectedDate)

                    runOnUiThread {
                        if (mood != null) {
                            val intent = Intent(this@CalendarActivity, MoodDetailActivity::class.java)
                            intent.putExtra("dailyMoodId", mood.dailyMoodId)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@CalendarActivity, MoodSelectionActivity::class.java)
                            intent.putExtra("date", selectedDate)
                            startActivityForResult(intent, REQUEST_MOOD_TAG)
                        }
                    }
                }
            }
        })
    }

    fun refreshCalendar() {
        loadMoodDates()
    }

    private fun loadMoodDates() {
        CoroutineScope(Dispatchers.IO).launch {
            val dailyMoods = db.dailyMoodDao().getAllByUser(currentUserId)
            val moodList = dailyMoods.map { dailyMood ->
                val mood = db.moodDao().getMoodById(dailyMood.moodId)
                Pair(dailyMood, mood)
            }

            val events: List<EventDay> = moodList.map { (dailyMood, mood) ->
                val cal = Calendar.getInstance().apply {
                    val parts = dailyMood.date.split("-")
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }

                val drawableRes = when (mood?.type) {
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

                EventDay(cal, drawableRes)
            }

            runOnUiThread {
                binding.calendarView.setEvents(events)

                // Show counts for current month
                val today = Calendar.getInstance()
                updateMoodCountsForMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1)
            }
        }
    }

    private fun updateMoodCountsForMonth(year: Int, month: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val dailyMoods = db.dailyMoodDao().getAllByUser(currentUserId)

            // Filter moods for this month/year
            val filteredMoods = dailyMoods.filter {
                val parts = it.date.split("-")
                val moodYear = parts[0].toInt()
                val moodMonth = parts[1].toInt()
                moodYear == year && moodMonth == month
            }

            // Count moods
            val moodCounts = mutableMapOf<String, Int>()
            for (dailyMood in filteredMoods) {
                val mood = db.moodDao().getMoodById(dailyMood.moodId)
                mood?.let {
                    moodCounts[it.type] = (moodCounts[it.type] ?: 0) + 1
                }
            }

            runOnUiThread {
                binding.moodCountLayout.removeAllViews()
                if (moodCounts.isEmpty()) {
                    val noData = android.widget.TextView(this@CalendarActivity)
                    noData.text = "No moods recorded this month"
                    noData.textSize = 16f
                    noData.setTextColor(resources.getColor(R.color.black))
                    binding.moodCountLayout.addView(noData)
                } else {
                    moodCounts.forEach { (type, count) ->
                        val tv = android.widget.TextView(this@CalendarActivity)
                        tv.text = "$type: $count"
                        tv.textSize = 16f
                        tv.setTextColor(resources.getColor(R.color.black))
                        binding.moodCountLayout.addView(tv)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MOOD_TAG && resultCode == RESULT_OK) {
            refreshCalendar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

}

