package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val reminderId = intent?.getLongExtra("reminder_id", -1L) ?: -1L
        val taskId = intent?.getLongExtra("task_id", -1L) ?: -1L
        if (reminderId == -1L && taskId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)

                if (taskId != -1L) {
                    val task = db.taskDao().getTaskById(taskId)
                    if (task != null && !task.isCompleted && task.hasReminder) {
                        val quote = db.quoteDao().getRandomQuote() ?: InitialData.initialQuotes.random()
                        NotificationHelper.showTaskReminderNotification(
                            context = context,
                            title = task.title,
                            description = task.description,
                            quoteText = quote.text,
                            quoteSource = quote.source,
                            taskId = task.id
                        )
                    }
                } else if (reminderId != -1L) {
                    val allReminders = db.reminderDao().getAllReminders().firstOrNull() ?: emptyList()
                    val reminder = allReminders.find { it.id == reminderId }

                    if (reminder != null && reminder.isEnabled) {
                        val quote = db.quoteDao().getRandomQuote() ?: InitialData.initialQuotes.random()
                        val enrichedNote = buildString {
                            if (reminder.note.isNotBlank()) {
                                append(reminder.note)
                                append("\n\n")
                            }
                            append("💬 \"")
                            append(quote.text)
                            append("\" — ")
                            append(quote.source)
                        }
                        NotificationHelper.showReminderNotification(
                            context = context,
                            title = reminder.title,
                            note = enrichedNote,
                            reminderId = reminder.id
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
