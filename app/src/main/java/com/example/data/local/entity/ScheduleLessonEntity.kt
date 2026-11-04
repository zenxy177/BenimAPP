package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_lessons")
data class ScheduleLessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int,                 // 1 = Pazartesi, 2 = Salı, 3 = Çarşamba, 4 = Perşembe, 5 = Cuma, 6 = Cumartesi
    val periodIndex: Int,               // 1 = 1. Ders, 2 = 2. Ders ...
    val lessonName: String,             // Örn: "Matematik"
    val teacherOrRoom: String = "",     // Örn: "Ahmet Hoca - B204"
    val startTime: String = "",         // Örn: "08:30"
    val endTime: String = "",           // Örn: "09:10"
    val colorHex: String = "#0D5C52"    // Kart rengi / rozet rengi
)
