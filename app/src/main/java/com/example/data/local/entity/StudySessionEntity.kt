package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [Index(value = ["dateString"])]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "courseOrTopic")
    val courseOrTopic: String,          // Örn: "Matematik Pomodoro"

    @ColumnInfo(name = "durationMinutes")
    val durationMinutes: Int,           // Örn: 25

    @ColumnInfo(name = "dateString")
    val dateString: String,             // YYYY-MM-DD

    @ColumnInfo(name = "completedAt")
    val completedAt: Long = System.currentTimeMillis()
)
