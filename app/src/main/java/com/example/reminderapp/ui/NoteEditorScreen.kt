package com.example.reminderapp.ui

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.Note
import com.example.reminderapp.data.NoteRepository
import com.example.reminderapp.notifications.ReminderReceiver
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(navController: NavController, repository: NoteRepository, noteId: Int?) {
    val context = navController.context
    val notes = remember { repository.getNotes().toMutableList() }
    val note = notes.find { it.id == noteId } ?: Note(
        id = notes.size + 1,
        title = "",
        content = ""
    )

    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var reminderTime by remember {
        mutableStateOf(note.reminderTime?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Изменить",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val updatedNote = note.copy(
                    title = title,
                    content = content,
                    reminderTime = reminderTime?.timeInMillis
                )
                if (notes.none { it.id == noteId }) {
                    notes.add(updatedNote)
                } else {
                    val index = notes.indexOfFirst { it.id == noteId }
                    notes[index] = updatedNote
                }
                repository.saveNotes(notes)

                // Установка напоминания, если выбрано время
                reminderTime?.let {
                    setReminder(context, updatedNote, it)
                }

                navController.popBackStack()
            }) {
                Text(text = "✔", style = MaterialTheme.typography.bodyLarge)
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(64.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // Передаем уже существующее напоминание или текущее время
                val initialTime = reminderTime ?: note.reminderTime?.let {
                    Calendar.getInstance().apply { timeInMillis = it }
                }

                showDateTimePicker(context, initialTime) { selectedTime ->
                    reminderTime = selectedTime
                }
            }) {
                Text("Поставить напоминание")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reminderTime?.time?.toString() ?: "Напоминание отсутствует",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

//Устанавливает напоминание с помощью AlarmManager
@SuppressLint("ScheduleExactAlarm")
private fun setReminder(context: Context, note: Note, reminderTime: Calendar) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Создаем Intent для напоминания
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("noteId", note.id) // Передаем идентификатор заметки
        putExtra("noteTitle", note.title)
        putExtra("noteContent", note.content)
    }

    // Создаем PendingIntent
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        note.id, // Уникальный ID для PendingIntent
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Устанавливаем напоминание
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        reminderTime.timeInMillis,
        pendingIntent
    )

    Toast.makeText(context, "Напоминание установлено на ${reminderTime.time}", Toast.LENGTH_SHORT).show()
}

//Показывает диалоги для выбора даты и времени
private fun showDateTimePicker(
    context: Context,
    initialTime: Calendar? = null, // Предустановленное время, если есть
    onDateTimeSelected: (Calendar) -> Unit
) {
    val currentTime = initialTime ?: Calendar.getInstance() // Если времени нет, берем текущее

    // DatePickerDialog для выбора даты
    DatePickerDialog(
        context,
        { _, year, month, day ->
            // После выбора даты открываем TimePickerDialog
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val selectedTime = Calendar.getInstance().apply {
                        set(year, month, day, hour, minute, 0)
                    }
                    onDateTimeSelected(selectedTime)
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
            ).show()
        },
        currentTime.get(Calendar.YEAR),
        currentTime.get(Calendar.MONTH),
        currentTime.get(Calendar.DAY_OF_MONTH)
    ).show()
}
