package com.example.reminderapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getNotes(): List<Note> {
        val json = sharedPreferences.getString("notes_list", null) ?: return emptyList()
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        sharedPreferences.edit().putString("notes_list", json).apply()
    }


}
