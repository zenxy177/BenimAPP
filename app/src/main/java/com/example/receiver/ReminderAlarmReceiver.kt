package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val reminderId = intent?.getLongExtra("reminder_id", -1L) ?: -1L
        if (reminderId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val allReminders = db.reminderDao().getAllReminders().firstOrNull() ?: emptyList()
                val reminder = allReminders.find { it.id == reminderId }

                if (reminder != null && reminder.isEnabled) {
                    NotificationHelper.showReminderNotification(
                        context = context,
                        title = reminder.title,
                        note = reminder.note,
                        reminderId = reminder.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
