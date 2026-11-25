package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskCategory {
    OKUL,
    KISISEL,
    MANEVIYAT,
    SAGLIK,
    GENEL
}

enum class TaskRecurrence {
    NONE,
    DAILY,
    WEEKLY
}

data class SubtaskItem(
    val title: String,
    val isDone: Boolean = false
)

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isCompleted"]),
        Index(value = ["category"]),
        Index(value = ["priority"]),
        Index(value = ["dueDateMillis"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "category")
    val category: TaskCategory = TaskCategory.OKUL,

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.ORTA,

    @ColumnInfo(name = "dueDateMillis")
    val dueDateMillis: Long? = null,

    @ColumnInfo(name = "hasReminder")
    val hasReminder: Boolean = false,

    @ColumnInfo(name = "recurrence")
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,

    @ColumnInfo(name = "subtasksRaw")
    val subtasksRaw: String = "", // Format: "0|Subtask 1;;1|Subtask 2"

    @ColumnInfo(name = "tags")
    val tags: String = "",

    @ColumnInfo(name = "focusedMinutes")
    val focusedMinutes: Int = 0,

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completedAt")
    val completedAt: Long? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parseSubtasks(): List<SubtaskItem> {
        if (subtasksRaw.isBlank()) return emptyList()
        return subtasksRaw.split(";;").mapNotNull { entry ->
            val trimmed = entry.trim()
            if (trimmed.isEmpty()) null
            else {
                val sepIndex = trimmed.indexOf('|')
                if (sepIndex != -1) {
                    val doneFlag = trimmed.substring(0, sepIndex) == "1"
                    val text = trimmed.substring(sepIndex + 1).trim()
                    if (text.isNotEmpty()) SubtaskItem(title = text, isDone = doneFlag) else null
                } else {
                    SubtaskItem(title = trimmed, isDone = false)
                }
            }
        }
    }

    companion object {
        fun encodeSubtasks(subtasks: List<SubtaskItem>): String {
            return subtasks
                .filter { it.title.isNotBlank() }
                .joinToString(";;") { item ->
                    val cleanTitle = item.title.replace(";;", " ").replace("|", "-")
                    "${if (item.isDone) "1" else "0"}|$cleanTitle"
                }
        }
    }
}
