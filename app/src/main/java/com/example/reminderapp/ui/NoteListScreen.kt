package com.example.reminderapp.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.NoteRepository
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
@Composable
fun NoteListScreen(navController: NavController, repository: NoteRepository) {
    var notes by remember { mutableStateOf(repository.getNotes().toMutableList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("noteEditor/-1") }) {
                Text(text = "+", style = MaterialTheme.typography.bodyLarge)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (notes.isEmpty()) {
                item {
                    Text(
                        text = "Заметок нет",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(notes.size) { index ->
                    val note = notes[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                navController.navigate("noteEditor/${note.id}")
                                }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.clickable {
                                    navController.navigate("noteEditor/${note.id}")
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Кнопка "Удалить"
                            Button(
                                onClick = {
                                    // Удаляем заметку из списка
                                    notes = notes.toMutableList().apply { removeAt(index) }
                                    repository.saveNotes(notes)

                                    // Показываем Snackbar
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Удалено")
                                    }
                                }
                            ) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
    }
}
