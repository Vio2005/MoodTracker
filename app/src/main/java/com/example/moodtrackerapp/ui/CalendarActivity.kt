package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.entity.DailyMoodEntity
import com.example.moodtrackerapp.databinding.ActivityCalendarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private var currentUserId: Long = -1L
    private val REQUEST_MOOD_TAG = 1001
    private var needsRefresh = false

    private val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load current user ID
        val sharedPref = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getLong("loggedInUserId", -1L)
        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(0, 0)
            return
        }

        // Go back to main
        binding.btnGoMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> true
                R.id.nav_profile -> {
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_calendar

        // Calendar setup
        refreshCalendar()
        binding.calendarView.post { disableFutureDates() }

        binding.calendarView.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() { updateCurrentPageMoodCounts() }
        })
        binding.calendarView.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() { updateCurrentPageMoodCounts() }
        })

        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val calendar = eventDay.calendar
                val today = Calendar.getInstance()
                if (calendar.after(today)) {
                    Toast.makeText(this@CalendarActivity, "Cannot select future date", Toast.LENGTH_SHORT).show()
                    return
                }

                // Format selected date
                val selectedDate = dateFormat.format(calendar.time)

                CoroutineScope(Dispatchers.IO).launch {
                    val mood: DailyMoodEntity? =
                        db.dailyMoodDao().getMoodByUserAndDate(currentUserId, selectedDate)

                    runOnUiThread {
                        if (mood != null) {
                            val intent = Intent(this@CalendarActivity, MoodDetailActivity::class.java)
                            intent.putExtra("dailyMoodId", mood.dailyMoodId)
                            startActivityForResult(intent, REQUEST_MOOD_TAG)
                            overridePendingTransition(0, 0)
                        } else {
                            // Pass the correct selected date to MoodSelectionActivity
                            val intent = Intent(this@CalendarActivity, MoodSelectionActivity::class.java)
                            intent.putExtra("date", selectedDate)
                            startActivityForResult(intent, REQUEST_MOOD_TAG)
                            overridePendingTransition(0, 0)
                        }
                    }
                }
            }
        })
    }

    fun refreshCalendar() {
        CoroutineScope(Dispatchers.IO).launch {
            val dailyMoods = db.dailyMoodDao().getAllByUser(currentUserId)
            val moodList = dailyMoods.mapNotNull { dailyMood ->
                val mood = db.moodDao().getMoodById(dailyMood.moodId)
                mood?.let { Pair(dailyMood, it.type) }
            }

            val events: List<EventDay> = moodList.map { (dailyMood, moodType) ->
                val cal = Calendar.getInstance().apply {
                    val parts = dailyMood.date.split("-")
                    if (parts.size == 3) set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }

                val drawableRes = when (moodType) {
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
                updateCurrentPageMoodCounts()
            }
        }
    }

    private fun updateCurrentPageMoodCounts() {
        val currentPage: Calendar = binding.calendarView.currentPageDate
        val year = currentPage.get(Calendar.YEAR)
        val month = currentPage.get(Calendar.MONTH) + 1
        updateMoodCountsForMonth(year, month)
    }

    private fun updateMoodCountsForMonth(year: Int, month: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val dailyMoods = db.dailyMoodDao().getAllByUser(currentUserId)
            val filteredMoods = dailyMoods.filter {
                val parts = it.date.split("-")
                parts.size == 3 && parts[0].toInt() == year && parts[1].toInt() == month
            }

            val moodCounts = mutableMapOf<String, Int>()
            for (dailyMood in filteredMoods) {
                val mood = db.moodDao().getMoodById(dailyMood.moodId)
                mood?.let { moodCounts[it.type] = (moodCounts[it.type] ?: 0) + 1 }
            }

            runOnUiThread {
                binding.moodCountLayout.removeAllViews()
                if (moodCounts.isEmpty()) {
                    val noData = TextView(this@CalendarActivity).apply {
                        text = "No moods recorded this month"
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.black))
                    }
                    binding.moodCountLayout.addView(noData)
                } else {
                    moodCounts.forEach { (type, count) ->
                        val tv = TextView(this@CalendarActivity).apply {
                            text = "$type: $count"
                            textSize = 16f
                            setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.black))
                        }
                        binding.moodCountLayout.addView(tv)
                    }
                }
            }
        }
    }

    private fun disableFutureDates() {
        val today = Calendar.getInstance()
        val disabledDates = mutableListOf<Calendar>()
        val cal = Calendar.getInstance()
        cal.time = today.time
        cal.add(Calendar.DAY_OF_MONTH, 1)

        for (i in 0 until 365) {
            disabledDates.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        binding.calendarView.setDisabledDays(disabledDates)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MOOD_TAG && resultCode == RESULT_OK) {
            needsRefresh = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (needsRefresh) {
            refreshCalendar()
            needsRefresh = false
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
