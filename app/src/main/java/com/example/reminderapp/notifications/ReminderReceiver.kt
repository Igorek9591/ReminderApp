package com.example.reminderapp.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.os.Build
import com.example.reminderapp.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Напоминание"
        val content = intent.getStringExtra("content") ?: "Время пришло!"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // Intent для открытия MainActivity
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateTo", "noteEditor")
            putExtra("noteId", intent.getIntExtra("noteId", -1))
            putExtra("reminderTime", intent.getLongExtra("reminderTime", -1L))
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )

        // Создаем уведомление
        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Устанавливаем PendingIntent
            .setAutoCancel(true)
            .build()

        // Отправляем уведомление
        with(NotificationManagerCompat.from(context)) {
            notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        }

    }
}
