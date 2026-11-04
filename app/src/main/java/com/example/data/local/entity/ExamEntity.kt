package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
    indices = [
        Index(value = ["examDateMillis"]),
        Index(value = ["courseName"])
    ]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "courseName")
    val courseName: String,             // Örn: "Kimya"

    @ColumnInfo(name = "examTitle")
    val examTitle: String,              // Örn: "1. Dönem 2. Yazılı Sınavı"

    @ColumnInfo(name = "examDateMillis")
    val examDateMillis: Long,           // Sınav tarihi ve saati

    @ColumnInfo(name = "topics")
    val topics: String = "",            // Sınavda çıkacak konular / notlar

    @ColumnInfo(name = "targetGrade")
    val targetGrade: Int? = null,       // Hedef not (örn: 95)

    @ColumnInfo(name = "actualGrade")
    val actualGrade: Int? = null,       // Alınan not (opsiyonel)

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
