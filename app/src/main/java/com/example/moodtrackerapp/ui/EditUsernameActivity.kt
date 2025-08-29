package com.example.moodtrackerapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditUsernameActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var btnSave: Button
    private val db by lazy { AppDatabase.getInstance(this) }
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_username)

        etUsername = findViewById(R.id.etUsername)
        btnSave = findViewById(R.id.btnSaveUsername)

        userId = intent.getLongExtra("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load current username
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getUserById(userId) }
            user?.let { etUsername.setText(it.username) }
        }

        btnSave.setOnClickListener {
            val newName = etUsername.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val user = db.userDao().getUserById(userId)
                if (user != null) {
                    val updatedUser = user.copy(username = newName)
                    db.userDao().updateUser(updatedUser)

                    // Return new username to MainActivity
                    val data = Intent().apply { putExtra("newUsername", newName) }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }
    }
}
