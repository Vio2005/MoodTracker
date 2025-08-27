package com.example.moodtrackerapp.model

data class MoodItem(val moodId: Int, val type: String, val icon: String?, val color: String?)

object Moods {
    val moodList = listOf(
        MoodItem(1, "Happy", "😊", "#FFD700"),
        MoodItem(2, "Sad", "😢", "#1E90FF"),
        MoodItem(3, "Angry", "😡", "#FF4500"),
        MoodItem(4, "Excited", "🤩", "#32CD32"),
        MoodItem(5, "Relaxed", "😌", "#8A2BE2")
    )
}
