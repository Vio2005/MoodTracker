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
import com.example.moodtrackerapp.databinding.ActivityLoginBinding
import com.example.moodtrackerapp.viewmodel.AuthViewModel
import com.example.moodtrackerapp.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(UserRepository(AppDatabase.getInstance(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRegisterText()

        // LOGIN BUTTON
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.loginUser(email, password)
        }

        // OBSERVE LOGIN RESULT
        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Welcome ${user.username}", Toast.LENGTH_SHORT).show()

                // Save logged-in user ID
                val prefs = getSharedPreferences("MoodAppPrefs", MODE_PRIVATE)
                prefs.edit().putLong("loggedInUserId", user.userId).apply() // mark logged-in

                // Go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

            result.onFailure {
                Toast.makeText(this, it.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        // REGISTER LINK
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setupRegisterText() {
        val text = "Don't have an account? Register"
        val spannable = SpannableString(text)

        // "Don't have an account?" in black
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            "Don't have an account? ".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // "Register" in green
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#03A081")), // Material Green
            "Don't have an account? ".length,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvRegisterLink.text = spannable
    }
}
