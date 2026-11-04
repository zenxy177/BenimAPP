package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
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

@Entity(
    tableName = "homeworks",
    indices = [
        Index(value = ["dueDateMillis"]),
        Index(value = ["type"]),
        Index(value = ["isCompleted"])
    ]
)
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "course")
    val course: String,                 // Örn: "Matematik", "Fizik", "Türk Dili ve Edebiyatı"

    @ColumnInfo(name = "type")
    val type: HomeworkType = HomeworkType.NORMAL,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "dueDateMillis")
    val dueDateMillis: Long,            // Teslim tarihi

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.ORTA,

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
