package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GoalCategory {
    AKADEMIK,       // Ders çalışma, soru çözme, tekrar
    MANEVIYAT,      // Kuran/Meal okuma, tefekkür, dua, zikir
    KISISISIL_GELISIM // Kitap okuma, spor, uyku düzeni
}

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                  // Örn: "50 Soru Çöz", "Kuran-ı Kerim Meal Oku"
    val category: GoalCategory = GoalCategory.AKADEMIK,
    val targetCount: Int = 1,           // Hedef sayı (örn: 50 soru veya 1 kere)
    val currentCount: Int = 0,          // Mevcut tamamlanan
    val unit: String = "adet",          // "soru", "sayfa", "dk", "kez"
    val isCompleted: Boolean = false,
    val dateString: String = "",        // YYYY-MM-DD formatında (günlük takip için)
    val createdAt: Long = System.currentTimeMillis()
)
