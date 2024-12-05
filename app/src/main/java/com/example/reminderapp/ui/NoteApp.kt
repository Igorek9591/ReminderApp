package com.example.reminderapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reminderapp.data.NoteRepository

@Composable
fun NoteApp(repository: NoteRepository, initialNoteId: Int? = null) {
    val navController = rememberNavController()

    // Если передан noteId, автоматически переходим к редактору заметок
    LaunchedEffect(initialNoteId) {
        initialNoteId?.let {
            navController.navigate("noteEditor/$it")
        }
    }

    NavHost(navController, startDestination = "noteList") {
        composable("noteList") {
            NoteListScreen(navController, repository)
        }
        composable("noteEditor/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            NoteEditorScreen(navController, repository, noteId)
        }
    }
}
