package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseName: String,             // Örn: "Kimya"
    val examTitle: String,              // Örn: "1. Dönem 2. Yazılı Sınavı"
    val examDateMillis: Long,           // Sınav tarihi ve saati
    val topics: String = "",            // Sınavda çıkacak konular / notlar
    val targetGrade: Int? = null,       // Hedef not (örn: 95)
    val actualGrade: Int? = null,       // Alınan not (opsiyonel)
    val createdAt: Long = System.currentTimeMillis()
)
