package com.example.reminderapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reminderapp.data.NoteRepository
import com.example.reminderapp.ui.NoteApp

class MainActivity : ComponentActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    // Обработчик результата для точных будильников
    private val scheduleExactAlarmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Разрешение предоставлено", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Проверяем разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestNotificationPermission()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequestExactAlarmPermission()
        }

        val navigateTo = intent.getStringExtra("navigateTo")
        val noteId = intent.getIntExtra("noteId", -1)

        setContent {
            MaterialTheme {
                NoteApp(
                    repository = NoteRepository(applicationContext),
                    initialNoteId = if (navigateTo == "noteEditor" && noteId != -1) noteId else null
                )
            }
        }
    }

     //Проверка и запрос разрешения на уведомления для Android 13+.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение уже предоставлено
            Toast.makeText(this, "Разрешение уже предоставлено", Toast.LENGTH_SHORT).show()
        } else {
            // Запрашиваем разрешение
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    //Проверка и запрос разрешения на точные будильники (Android 12+).
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestExactAlarmPermission() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            scheduleExactAlarmResultLauncher.launch(intent)
        }
    }

    //Создание канала уведомлений для напоминаний.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "reminder_channel"
            val channelName = "Reminder Notifications"
            val channelDescription = "Channel for note reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}