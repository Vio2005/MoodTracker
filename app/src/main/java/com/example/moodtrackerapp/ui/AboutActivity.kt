package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.moodtrackerapp.R
import com.google.android.material.imageview.ShapeableImageView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_view)

    // link your XML here
        val backIcon = findViewById<ShapeableImageView>(R.id.back_icon)


        backIcon.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // ensures AboutActivity is removed
        }
    }
}
