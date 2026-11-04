package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GoalCategory {
    AKADEMIK,        // Ders çalışma, soru çözme, tekrar
    MANEVIYAT,       // Kuran/Meal okuma, tefekkür, dua, zikir
    KISISISIL_GELISIM // Kitap okuma, spor, uyku düzeni
}

@Entity(
    tableName = "daily_goals",
    indices = [
        Index(value = ["dateString"]),
        Index(value = ["category"]),
        Index(value = ["isCompleted"])
    ]
)
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,                  // Örn: "50 Soru Çöz", "Kuran-ı Kerim Meal Oku"

    @ColumnInfo(name = "category")
    val category: GoalCategory = GoalCategory.AKADEMIK,

    @ColumnInfo(name = "targetCount")
    val targetCount: Int = 1,           // Hedef sayı (örn: 50 soru veya 1 kere)

    @ColumnInfo(name = "currentCount")
    val currentCount: Int = 0,          // Mevcut tamamlanan

    @ColumnInfo(name = "unit")
    val unit: String = "adet",          // "soru", "sayfa", "dk", "kez"

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "dateString")
    val dateString: String = "",        // YYYY-MM-DD formatında (günlük takip için)

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
