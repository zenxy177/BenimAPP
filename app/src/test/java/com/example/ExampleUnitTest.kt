package com.example

import com.example.data.local.InitialData
import com.example.data.local.entity.GoalCategory
import com.example.data.local.entity.HomeworkType
import com.example.data.local.entity.QuoteCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
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
    val goals = InitialData.getSampleGoals("2026-11-03")
    assertTrue(goals.isNotEmpty())
    assertTrue(goals.any { it.category == GoalCategory.AKADEMIK })
    assertTrue(goals.any { it.category == GoalCategory.MANEVIYAT })
  }

  @Test
  fun testSampleScheduleDays() {
    val schedule = InitialData.getSampleSchedule()
    assertTrue(schedule.isNotEmpty())
    assertTrue(schedule.any { it.dayOfWeek == 1 }) // Pazartesi
    assertTrue(schedule.any { it.dayOfWeek == 5 }) // Cuma
  }
}
