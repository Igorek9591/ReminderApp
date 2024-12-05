package com.example.reminderapp.data

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val reminderTime: Long? = null // Время напоминания в миллисекундах
)