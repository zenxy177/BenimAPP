package com.example

import com.example.data.local.InitialData
import com.example.data.local.RoomConverters
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.HomeworkType
import com.example.data.local.entity.Priority
import com.example.data.local.entity.QuoteCategory
import com.example.data.local.entity.SubtaskItem
import com.example.data.local.entity.TaskCategory
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskRecurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val converters = RoomConverters()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRoomTypeConverters() {
        HomeworkType.values().forEach { type ->
            val serialized = converters.fromHomeworkType(type)
            assertEquals(type, converters.toHomeworkType(serialized))
        }
        Priority.values().forEach { priority ->
            val serialized = converters.fromPriority(priority)
            assertEquals(priority, converters.toPriority(serialized))
        }
        GoalCategory.values().forEach { category ->
            val serialized = converters.fromGoalCategory(category)
            assertEquals(category, converters.toGoalCategory(serialized))
        }
        QuoteCategory.values().forEach { category ->
            val serialized = converters.fromQuoteCategory(category)
            assertEquals(category, converters.toQuoteCategory(serialized))
        }
        TaskCategory.values().forEach { category ->
            val serialized = converters.fromTaskCategory(category)
            assertEquals(category, converters.toTaskCategory(serialized))
        }
        TaskRecurrence.values().forEach { rec ->
            val serialized = converters.fromTaskRecurrence(rec)
            assertEquals(rec, converters.toTaskRecurrence(serialized))
        }
    }

    @Test
    fun testTaskSubtasksSerialization() {
        val subtasks = listOf(
            SubtaskItem("Konu tekrarı yap", true),
            SubtaskItem("20 soru çöz", false)
        )
        val encoded = TaskEntity.encodeSubtasks(subtasks)
        val task = TaskEntity(title = "Test Görev", subtasksRaw = encoded)
        val parsed = task.parseSubtasks()
        assertEquals(2, parsed.size)
        assertEquals("Konu tekrarı yap", parsed[0].title)
        assertTrue(parsed[0].isDone)
        assertEquals("20 soru çöz", parsed[1].title)
        assertFalse(parsed[1].isDone)
    }

    @Test
    fun testSampleTasks() {
        val tasks = InitialData.getSampleTasks()
        assertTrue(tasks.isNotEmpty())
        assertTrue(tasks.any { it.category == TaskCategory.OKUL })
        assertTrue(tasks.any { it.parseSubtasks().isNotEmpty() })
    }

    @Test
    fun testInitialQuotesCategories() {
        val quotes = InitialData.initialQuotes
        assertTrue(quotes.isNotEmpty())
        assertTrue(quotes.any { it.category == QuoteCategory.AYET })
        assertTrue(quotes.any { it.category == QuoteCategory.HADIS })
        assertTrue(quotes.any { it.category == QuoteCategory.HIKMET })
    }

    @Test
    fun testSampleHomeworkTypes() {
        val homeworks = InitialData.getSampleHomeworks()
        assertTrue(homeworks.isNotEmpty())
        assertTrue(homeworks.any { it.type == HomeworkType.NORMAL })
        assertTrue(homeworks.any { it.type == HomeworkType.PERFORMANS })
        assertTrue(homeworks.any { it.type == HomeworkType.PROJE })
    }

    @Test
    fun testSampleGoals() {
        val goals = InitialData.getSampleGoals("2026-11-04")
        assertTrue(goals.isNotEmpty())
        assertTrue(goals.any { it.category == GoalCategory.AKADEMIK })
        assertTrue(goals.any { it.category == GoalCategory.MANEVIYAT })
    }

    @Test
    fun testSampleScheduleDays() {
        val schedule = InitialData.getSampleSchedule()
        assertTrue(schedule.isNotEmpty())
        assertTrue(schedule.any { it.dayOfWeek == 1 })
        assertTrue(schedule.any { it.dayOfWeek == 5 })
    }

    @Test
    fun testInitialDhikrs() {
        val dhikrs = InitialData.initialDhikrs
        assertTrue(dhikrs.isNotEmpty())
        assertTrue(dhikrs.all { it.targetCount > 0 })
    }

    @Test
    fun testInitialStudyNotes() {
        val notes = InitialData.initialStudyNotes
        assertTrue(notes.isNotEmpty())
        assertTrue(notes.all { it.title.isNotBlank() && it.content.isNotBlank() })
    }
}
