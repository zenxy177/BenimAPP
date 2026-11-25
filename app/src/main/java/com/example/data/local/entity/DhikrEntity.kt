package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_items")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,                  // Örn: "Sübhanallah"

    @ColumnInfo(name = "arabicOrMeaning")
    val arabicOrMeaning: String = "",   // Örn: "Allah her türlü eksiklikten münezzehtir"

    @ColumnInfo(name = "currentCount")
    val currentCount: Int = 0,          // Mevcut tur sayacı

    @ColumnInfo(name = "targetCount")
    val targetCount: Int = 33,          // Hedef (33, 99, 100 vb.)

    @ColumnInfo(name = "totalLifetimeCount")
    val totalLifetimeCount: Int = 0     // Toplam çekilen tesbihat
)
