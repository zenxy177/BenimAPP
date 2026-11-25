package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_notes",
    indices = [Index(value = ["courseName"])]
)
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "courseName")
    val courseName: String,             // Örn: "Matematik", "Tarih", "Fizik"

    @ColumnInfo(name = "title")
    val title: String,                  // Soru / Kavram / Formül Başlığı

    @ColumnInfo(name = "content")
    val content: String,                // Cevap / Açıklama / Detay

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
