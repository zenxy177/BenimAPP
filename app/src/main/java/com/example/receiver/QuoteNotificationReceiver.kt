package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.local.entity.QuoteCategory
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val quote = db.quoteDao().getRandomQuote() ?: InitialData.initialQuotes.random()

                val categoryTitle = when (quote.category) {
                    QuoteCategory.AYET -> "Günün Âyeti Kerîmesi"
                    QuoteCategory.HADIS -> "Günün Hadîs-i Şerîfi"
                    QuoteCategory.HIKMET -> "Günün Hikmetli Sözü"
                }

                NotificationHelper.showQuoteNotification(
                    context = context,
                    text = quote.text,
                    source = quote.source,
                    categoryTitle = categoryTitle
                )
            } catch (e: Exception) {
                val fallback = InitialData.initialQuotes.first()
                NotificationHelper.showQuoteNotification(
                    context = context,
                    text = fallback.text,
                    source = fallback.source,
                    categoryTitle = "Günün İlhamı"
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
