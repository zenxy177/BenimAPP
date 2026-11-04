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
    private val homeworkDao = db.homeworkDao()
    private val examDao = db.examDao()
    private val goalDao = db.goalDao()
    private val quoteDao = db.quoteDao()
    private val scheduleDao = db.scheduleDao()
    private val reminderDao = db.reminderDao()

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Initialize database data if empty
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        if (quoteDao.getQuoteCount() == 0) {
            quoteDao.insertQuotes(InitialData.initialQuotes)
        }
        if (scheduleDao.getScheduleCount() == 0) {
            scheduleDao.insertLessons(InitialData.getSampleSchedule())
        }
        val todayStr = getTodayDateString()
        if (goalDao.getGoalCountForDate(todayStr) == 0) {
            goalDao.insertGoals(InitialData.getSampleGoals(todayStr))
        }
        if (homeworkDao.getHomeworkCount() == 0) {
            homeworkDao.insertHomeworks(InitialData.getSampleHomeworks())
        }
        if (examDao.getExamCount() == 0) {
            examDao.insertExams(InitialData.getSampleExams())
        }
    }

    // --- QUOTES (QuoteDao) ---
    val allQuotes: Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()

    suspend fun toggleFavoriteQuote(quote: QuoteEntity) = withContext(Dispatchers.IO) {
        quoteDao.updateQuote(quote.copy(isFavorite = !quote.isFavorite))
    }

    suspend fun addQuote(text: String, source: String, category: QuoteCategory) = withContext(Dispatchers.IO) {
        val newQuote = QuoteEntity(
            text = text.trim(),
            source = source.trim().ifBlank { "Anonim" },
            category = category,
            isUserAdded = true
        )
        quoteDao.insertQuote(newQuote)
    }

    suspend fun deleteQuote(quote: QuoteEntity) = withContext(Dispatchers.IO) {
        quoteDao.deleteQuote(quote)
    }

    suspend fun getRandomQuote(): QuoteEntity? = withContext(Dispatchers.IO) {
        quoteDao.getRandomQuote()
    }

    // --- HOMEWORKS (HomeworkDao) ---
    val allHomeworks: Flow<List<HomeworkEntity>> = homeworkDao.getAllHomeworks()

    fun getHomeworksByType(type: HomeworkType): Flow<List<HomeworkEntity>> =
        homeworkDao.getHomeworksByType(type)

    fun getPendingHomeworks(): Flow<List<HomeworkEntity>> =
        homeworkDao.getPendingHomeworks()

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
        homeworkDao.insertHomework(hw)
    }

    suspend fun toggleHomework(homework: HomeworkEntity) = withContext(Dispatchers.IO) {
        homeworkDao.updateHomework(homework.copy(isCompleted = !homework.isCompleted))
    }

    suspend fun deleteHomework(homework: HomeworkEntity) = withContext(Dispatchers.IO) {
        homeworkDao.deleteHomework(homework)
    }

    // --- EXAMS (ExamDao) ---
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()

    fun getUpcomingExams(currentTimeMillis: Long = System.currentTimeMillis()): Flow<List<ExamEntity>> =
        examDao.getUpcomingExams(currentTimeMillis)

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
        examDao.insertExam(exam)
    }

    suspend fun updateExamGrade(exam: ExamEntity, actualGrade: Int?) = withContext(Dispatchers.IO) {
        examDao.updateExam(exam.copy(actualGrade = actualGrade))
    }

    suspend fun deleteExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        examDao.deleteExam(exam)
    }

    // --- SCHEDULE (ScheduleDao) ---
    val allLessons: Flow<List<ScheduleLessonEntity>> = scheduleDao.getAllLessons()

    fun getLessonsForDay(dayOfWeek: Int): Flow<List<ScheduleLessonEntity>> =
        scheduleDao.getLessonsForDay(dayOfWeek)

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
        scheduleDao.insertLesson(lesson)
    }

    suspend fun deleteLesson(lesson: ScheduleLessonEntity) = withContext(Dispatchers.IO) {
        scheduleDao.deleteLesson(lesson)
    }

    // --- DAILY GOALS (GoalDao) ---
    fun getTodayGoals(): Flow<List<DailyGoalEntity>> =
        goalDao.getGoalsForDate(getTodayDateString())

    fun getGoalsByDateAndCategory(category: GoalCategory): Flow<List<DailyGoalEntity>> =
        goalDao.getGoalsByDateAndCategory(getTodayDateString(), category)

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
        goalDao.insertGoal(goal)
    }

    suspend fun incrementGoal(goal: DailyGoalEntity, delta: Int = 1) = withContext(Dispatchers.IO) {
        val newCurrent = (goal.currentCount + delta).coerceAtLeast(0)
        val isDone = newCurrent >= goal.targetCount
        goalDao.updateGoal(goal.copy(currentCount = newCurrent, isCompleted = isDone))
    }

    suspend fun toggleGoalCompleted(goal: DailyGoalEntity) = withContext(Dispatchers.IO) {
        val isDone = !goal.isCompleted
        val newCurrent = if (isDone) goal.targetCount else 0
        goalDao.updateGoal(goal.copy(isCompleted = isDone, currentCount = newCurrent))
    }

    suspend fun deleteGoal(goal: DailyGoalEntity) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(goal)
    }

    // --- REMINDERS (ReminderDao) ---
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun addReminder(title: String, note: String, triggerTimeMillis: Long) = withContext(Dispatchers.IO) {
        val rem = ReminderEntity(
            title = title.trim(),
            note = note.trim(),
            triggerTimeMillis = triggerTimeMillis,
            isEnabled = true
        )
        val id = reminderDao.insertReminder(rem)
        NotificationHelper.scheduleExactReminder(context, id, triggerTimeMillis)
    }

    suspend fun toggleReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        val newEnabled = !reminder.isEnabled
        reminderDao.updateReminder(reminder.copy(isEnabled = newEnabled))
        if (newEnabled) {
            NotificationHelper.scheduleExactReminder(context, reminder.id, reminder.triggerTimeMillis)
        } else {
            NotificationHelper.cancelReminder(context, reminder.id)
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelReminder(context, reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    // --- NOTIFICATION UTILITIES ---
    suspend fun triggerTestQuoteNotification() = withContext(Dispatchers.IO) {
        val quote = quoteDao.getRandomQuote() ?: InitialData.initialQuotes.random()
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
