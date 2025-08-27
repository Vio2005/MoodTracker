package com.example.moodtrackerapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.moodtrackerapp.data.AppDatabase
import com.example.moodtrackerapp.data.UserRepository
import com.example.moodtrackerapp.databinding.ActivityRegisterBinding
import com.example.moodtrackerapp.viewmodel.AuthViewModel
import com.example.moodtrackerapp.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepository(AppDatabase.getInstance(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginText()

        // REGISTER BUTTON
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.registerUser(email, password, username)
        }

        // OBSERVE REGISTRATION RESULT
        authViewModel.registerResult.observe(this) { result ->
            result.onSuccess { user ->
                // Save logged-in user ID
                val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
                prefs.edit().putLong("loggedInUserId", user.userId).apply() // mark logged-in

                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()

                // Go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            result.onFailure {
                Toast.makeText(this, it.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        // LOGIN LINK
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupLoginText() {
        val text = "Already have an account? Login"
        val spannable = SpannableString(text)

        // "Already have an account?" in black
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            "Already have an account? ".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // "Login" in green
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#459C07")), // Material Green
            "Already have an account? ".length,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvLoginLink.text = spannable
    }
}
