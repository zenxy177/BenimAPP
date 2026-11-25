package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.QuoteNotificationReceiver
import com.example.receiver.ReminderAlarmReceiver

object NotificationHelper {

    const val CHANNEL_QUOTES = "channel_quotes_wisdom"
    const val CHANNEL_REMINDERS = "channel_reminders_tasks"

    const val NOTIFICATION_ID_QUOTE = 1001
    const val REQUEST_CODE_PERIODIC_QUOTE = 2001

    private fun pendingIntentFlags(baseFlags: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            baseFlags or PendingIntent.FLAG_IMMUTABLE
        } else {
            baseFlags
        }
    }

    fun createNotificationChannels(context: Context) {
        // NotificationChannel is only required on Android 8.0+ (API 26+)
        // On Android 7.0 Nougat (API 24, Vestel Venus V4), NotificationCompat handles priority & defaults directly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val quotesChannel = NotificationChannel(
                CHANNEL_QUOTES,
                "Günün Sözü ve Hikmetler",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ayet-i kerimeler, hadis-i şerifler ve tarihten ilham veren hikmetli sözler"
                enableVibration(true)
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Hatırlatıcılar ve Ödevler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Planladığınız kişisel hatırlatıcılar ve ödev alarmları"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(quotesChannel)
            manager?.createNotificationChannel(remindersChannel)
        }
    }

    fun showQuoteNotification(context: Context, text: String, source: String, categoryTitle: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "quotes")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_QUOTE,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_QUOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✨ $categoryTitle")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"$text\"\n\n— $source"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(NOTIFICATION_ID_QUOTE, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showReminderNotification(context: Context, title: String, note: String, reminderId: Long) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reminders")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val contentNote = if (note.isNotBlank()) note else "Zamanı geldi!"

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Hatırlatıcı: $title")
            .setContentText(contentNote)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n$contentNote"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(reminderId.toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun schedulePeriodicQuoteNotification(context: Context, intervalHours: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, QuoteNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PERIODIC_QUOTE,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val intervalMillis = intervalHours.toLong() * 60L * 60L * 1000L
        val triggerTime = System.currentTimeMillis() + intervalMillis

        try {
            // setInexactRepeating batches wakeup alarms on Android 7.0+ to save battery and RAM
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun cancelPeriodicQuoteNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, QuoteNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PERIODIC_QUOTE,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_NO_CREATE)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showTaskReminderNotification(
        context: Context,
        title: String,
        description: String,
        quoteText: String,
        quoteSource: String,
        taskId: Long
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "reminders")
        }
        val requestCode = 50000 + taskId.toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val summaryLine = if (description.isNotBlank()) description else "Planladığın görevi tamamlama vakti geldi!"
        val bigBody = buildString {
            append(summaryLine)
            if (quoteText.isNotBlank()) {
                append("\n\n💬 \"")
                append(quoteText)
                append("\"")
                if (quoteSource.isNotBlank()) {
                    append(" — ")
                    append(quoteSource)
                }
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎯 Görev Vakti: $title")
            .setContentText(summaryLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(requestCode, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun scheduleExactReminder(context: Context, reminderId: Long, triggerMillis: Long) {
        if (triggerMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        try {
            // AlarmManagerCompat safely handles API 24 (Android 7.0 Nougat) through API 36
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            pendingIntentFlags(PendingIntent.FLAG_NO_CREATE)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleTaskReminder(context: Context, taskId: Long, triggerMillis: Long) {
        if (triggerMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = 50000 + taskId.toInt()
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("task_id", taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        try {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = 50000 + taskId.toInt()
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            pendingIntentFlags(PendingIntent.FLAG_NO_CREATE)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
