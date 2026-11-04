package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.HomeworkType
import com.example.data.local.entity.Priority
import com.example.data.local.entity.QuoteCategory

class RoomConverters {

    @TypeConverter
    fun fromHomeworkType(value: HomeworkType): String = value.name

    @TypeConverter
    fun toHomeworkType(value: String): HomeworkType =
        runCatching { HomeworkType.valueOf(value) }.getOrDefault(HomeworkType.NORMAL)

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        runCatching { Priority.valueOf(value) }.getOrDefault(Priority.ORTA)

    @TypeConverter
    fun fromGoalCategory(value: GoalCategory): String = value.name

    @TypeConverter
    fun toGoalCategory(value: String): GoalCategory =
        runCatching { GoalCategory.valueOf(value) }.getOrDefault(GoalCategory.AKADEMIK)

    @TypeConverter
    fun fromQuoteCategory(value: QuoteCategory): String = value.name

    @TypeConverter
    fun toQuoteCategory(value: String): QuoteCategory =
        runCatching { QuoteCategory.valueOf(value) }.getOrDefault(QuoteCategory.HIKMET)
}
