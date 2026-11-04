package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.local.entity.*
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.appDao()

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Initialize database data if empty
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        if (dao.getQuoteCount() == 0) {
            dao.insertQuotes(InitialData.initialQuotes)
        }
        if (dao.getScheduleCount() == 0) {
            dao.insertLessons(InitialData.getSampleSchedule())
        }
        val todayStr = getTodayDateString()
        if (dao.getGoalCountForDate(todayStr) == 0) {
            dao.insertGoals(InitialData.getSampleGoals(todayStr))
        }
        // Seed homeworks if empty
        val existingHw = dao.getAllHomeworks()
        // Default seed is done in callback or here
    }

    // --- QUOTES ---
    val allQuotes: Flow<List<QuoteEntity>> = dao.getAllQuotes()

    suspend fun toggleFavoriteQuote(quote: QuoteEntity) = withContext(Dispatchers.IO) {
        dao.updateQuote(quote.copy(isFavorite = !quote.isFavorite))
    }

    suspend fun addQuote(text: String, source: String, category: QuoteCategory) = withContext(Dispatchers.IO) {
        val newQuote = QuoteEntity(
            text = text.trim(),
            source = source.trim().ifBlank { "Anonim" },
            category = category,
            isUserAdded = true
        )
        dao.insertQuote(newQuote)
    }

    suspend fun deleteQuote(quote: QuoteEntity) = withContext(Dispatchers.IO) {
        dao.deleteQuote(quote)
    }

    suspend fun getRandomQuote(): QuoteEntity? = withContext(Dispatchers.IO) {
        dao.getRandomQuote()
    }

    // --- HOMEWORKS ---
    val allHomeworks: Flow<List<HomeworkEntity>> = dao.getAllHomeworks()

    suspend fun addHomework(
        title: String,
        course: String,
        type: HomeworkType,
        description: String,
        dueDateMillis: Long,
        priority: Priority
    ) = withContext(Dispatchers.IO) {
        val hw = HomeworkEntity(
            title = title.trim(),
            course = course.trim(),
            type = type,
            description = description.trim(),
            dueDateMillis = dueDateMillis,
            priority = priority
        )
        dao.insertHomework(hw)
    }

    suspend fun toggleHomework(homework: HomeworkEntity) = withContext(Dispatchers.IO) {
        dao.updateHomework(homework.copy(isCompleted = !homework.isCompleted))
    }

    suspend fun deleteHomework(homework: HomeworkEntity) = withContext(Dispatchers.IO) {
        dao.deleteHomework(homework)
    }

    // --- EXAMS ---
    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()

    suspend fun addExam(
        courseName: String,
        examTitle: String,
        examDateMillis: Long,
        topics: String,
        targetGrade: Int?
    ) = withContext(Dispatchers.IO) {
        val exam = ExamEntity(
            courseName = courseName.trim(),
            examTitle = examTitle.trim(),
            examDateMillis = examDateMillis,
            topics = topics.trim(),
            targetGrade = targetGrade
        )
        dao.insertExam(exam)
    }

    suspend fun updateExamGrade(exam: ExamEntity, actualGrade: Int?) = withContext(Dispatchers.IO) {
        dao.updateExam(exam.copy(actualGrade = actualGrade))
    }

    suspend fun deleteExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        dao.deleteExam(exam)
    }

    // --- SCHEDULE ---
    val allLessons: Flow<List<ScheduleLessonEntity>> = dao.getAllLessons()

    fun getLessonsForDay(dayOfWeek: Int): Flow<List<ScheduleLessonEntity>> = dao.getLessonsForDay(dayOfWeek)

    suspend fun addLesson(
        dayOfWeek: Int,
        periodIndex: Int,
        lessonName: String,
        teacherOrRoom: String,
        startTime: String,
        endTime: String,
        colorHex: String
    ) = withContext(Dispatchers.IO) {
        val lesson = ScheduleLessonEntity(
            dayOfWeek = dayOfWeek,
            periodIndex = periodIndex,
            lessonName = lessonName.trim(),
            teacherOrRoom = teacherOrRoom.trim(),
            startTime = startTime.trim(),
            endTime = endTime.trim(),
            colorHex = colorHex
        )
        dao.insertLesson(lesson)
    }

    suspend fun deleteLesson(lesson: ScheduleLessonEntity) = withContext(Dispatchers.IO) {
        dao.deleteLesson(lesson)
    }

    // --- DAILY GOALS ---
    fun getTodayGoals(): Flow<List<DailyGoalEntity>> = dao.getGoalsForDate(getTodayDateString())

    suspend fun addGoal(
        title: String,
        category: GoalCategory,
        targetCount: Int,
        unit: String
    ) = withContext(Dispatchers.IO) {
        val goal = DailyGoalEntity(
            title = title.trim(),
            category = category,
            targetCount = if (targetCount > 0) targetCount else 1,
            currentCount = 0,
            unit = unit.trim().ifBlank { "adet" },
            isCompleted = false,
            dateString = getTodayDateString()
        )
        dao.insertGoal(goal)
    }

    suspend fun incrementGoal(goal: DailyGoalEntity, delta: Int = 1) = withContext(Dispatchers.IO) {
        val newCurrent = (goal.currentCount + delta).coerceAtLeast(0)
        val isDone = newCurrent >= goal.targetCount
        dao.updateGoal(goal.copy(currentCount = newCurrent, isCompleted = isDone))
    }

    suspend fun toggleGoalCompleted(goal: DailyGoalEntity) = withContext(Dispatchers.IO) {
        val isDone = !goal.isCompleted
        val newCurrent = if (isDone) goal.targetCount else 0
        dao.updateGoal(goal.copy(isCompleted = isDone, currentCount = newCurrent))
    }

    suspend fun deleteGoal(goal: DailyGoalEntity) = withContext(Dispatchers.IO) {
        dao.deleteGoal(goal)
    }

    // --- REMINDERS ---
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun addReminder(title: String, note: String, triggerTimeMillis: Long) = withContext(Dispatchers.IO) {
        val rem = ReminderEntity(
            title = title.trim(),
            note = note.trim(),
            triggerTimeMillis = triggerTimeMillis,
            isEnabled = true
        )
        val id = dao.insertReminder(rem)
        NotificationHelper.scheduleExactReminder(context, id, triggerTimeMillis)
    }

    suspend fun toggleReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        val newEnabled = !reminder.isEnabled
        dao.updateReminder(reminder.copy(isEnabled = newEnabled))
        if (newEnabled) {
            NotificationHelper.scheduleExactReminder(context, reminder.id, reminder.triggerTimeMillis)
        } else {
            NotificationHelper.cancelReminder(context, reminder.id)
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelReminder(context, reminder.id)
        dao.deleteReminder(reminder)
    }

    // --- NOTIFICATION UTILITIES ---
    suspend fun triggerTestQuoteNotification() = withContext(Dispatchers.IO) {
        val quote = dao.getRandomQuote() ?: InitialData.initialQuotes.random()
        val categoryTitle = when (quote.category) {
            QuoteCategory.AYET -> "Âyet-i Kerîme"
            QuoteCategory.HADIS -> "Hadîs-i Şerîf"
            QuoteCategory.HIKMET -> "Hikmetli Söz"
        }
        NotificationHelper.showQuoteNotification(
            context = context,
            text = quote.text,
            source = quote.source,
            categoryTitle = categoryTitle
        )
    }
}
