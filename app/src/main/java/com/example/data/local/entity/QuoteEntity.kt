package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QuoteCategory {
    AYET,       // Allah'ın Sözleri (Kuran-ı Kerim Mealleri)
    HADIS,      // Peygamber Efendimiz'in (S.A.V.) Sözleri
    HIKMET      // Tarihe Geçmiş Önemli Düşünür ve Alim Sözleri
}

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val source: String,          // Örn: "Bakara Sûresi, 152", "Buhârî, İlim 10", "Hz. Ali (r.a.)"
    val category: QuoteCategory,
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
