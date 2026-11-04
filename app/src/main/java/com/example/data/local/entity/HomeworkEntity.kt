package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HomeworkType {
    NORMAL,         // Normal Ödev
    PERFORMANS,     // Performans Görevi
    PROJE           // Proje Ödevi
}

enum class Priority {
    DUSUK,
    ORTA,
    YUKSEK
}

@Entity(tableName = "homeworks")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val course: String,                 // Örn: "Matematik", "Fizik", "Türk Dili ve Edebiyatı"
    val type: HomeworkType = HomeworkType.NORMAL,
    val description: String = "",
    val dueDateMillis: Long,            // Teslim tarihi
    val priority: Priority = Priority.ORTA,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
