package com.example.moodtrackerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import com.example.moodtrackerapp.R


class ModeActivity : AppCompatActivity() {

    private lateinit var lightOption: LinearLayout
    private lateinit var darkOption: LinearLayout
    private lateinit var systemDefaultOption: LinearLayout

    private lateinit var lightRadio: RadioButton
    private lateinit var darkRadio: RadioButton
    private lateinit var systemDefaultRadio: RadioButton

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mode_view) // this is your mode_view.xml


        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        lightOption = findViewById(R.id.lightOption)
        darkOption = findViewById(R.id.darkOption)
        systemDefaultOption = findViewById(R.id.systemDefaultOption)

        lightRadio = findViewById(R.id.lightRadio)
        darkRadio = findViewById(R.id.darkRadio)
        systemDefaultRadio = findViewById(R.id.systemDefaultRadio)

        // Load saved theme
        when (sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_NO -> lightRadio.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> darkRadio.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> systemDefaultRadio.isChecked = true
        }

        lightOption.setOnClickListener { setThemeMode(AppCompatDelegate.MODE_NIGHT_NO) }
        darkOption.setOnClickListener { setThemeMode(AppCompatDelegate.MODE_NIGHT_YES) }
        systemDefaultOption.setOnClickListener { setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
    }

    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)

        // Save selection
        sharedPreferences.edit().putInt("theme_mode", mode).apply()

        // Update radio buttons
        lightRadio.isChecked = mode == AppCompatDelegate.MODE_NIGHT_NO
        darkRadio.isChecked = mode == AppCompatDelegate.MODE_NIGHT_YES
        systemDefaultRadio.isChecked = mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}

