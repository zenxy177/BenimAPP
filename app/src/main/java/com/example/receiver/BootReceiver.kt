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

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Reschedule periodic quotes (default 6 hours)
                    NotificationHelper.schedulePeriodicQuoteNotification(context, 6)

                    // Reschedule any future reminders
                    val db = AppDatabase.getDatabase(context)
                    val reminders = db.appDao().getAllReminders().firstOrNull() ?: emptyList()
                    val now = System.currentTimeMillis()
                    reminders.filter { it.isEnabled && it.triggerTimeMillis > now }.forEach { rem ->
                        NotificationHelper.scheduleExactReminder(context, rem.id, rem.triggerTimeMillis)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
