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
    private val dhikrDao = db.dhikrDao()
    private val studyNoteDao = db.studyNoteDao()
    private val studySessionDao = db.studySessionDao()
    private val taskDao = db.taskDao()

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("rehberim_prefs", Context.MODE_PRIVATE)
        val todayStr = getTodayDateString()
        val alreadySeeded = prefs.getBoolean("is_database_seeded", false)
        val tasksSeeded = prefs.getBoolean("is_tasks_seeded", false)

        if (!tasksSeeded) {
            if (taskDao.getTaskCount() == 0) {
                val sampleTasks = InitialData.getSampleTasks()
                taskDao.insertTasks(sampleTasks)
            }
            prefs.edit().putBoolean("is_tasks_seeded", true).apply()
        }

        if (alreadySeeded) {
            handleDailyGoalRollover(todayStr, prefs)
            return@withContext
        }

        // Check if database already has user or seeded records from earlier executions
        val hasExistingData = quoteDao.getQuoteCount() > 0 ||
            homeworkDao.getHomeworkCount() > 0 ||
            scheduleDao.getScheduleCount() > 0 ||
            dhikrDao.getDhikrCount() > 0 ||
            studyNoteDao.getStudyNoteCount() > 0

        if (hasExistingData) {
            prefs.edit()
                .putBoolean("is_database_seeded", true)
                .putBoolean("is_tasks_seeded", true)
                .putString("last_goal_rollover_date", todayStr)
                .apply()
            handleDailyGoalRollover(todayStr, prefs)
            return@withContext
        }

        // First clean app install: seed initial data once
        quoteDao.insertQuotes(InitialData.initialQuotes)
        scheduleDao.insertLessons(InitialData.getSampleSchedule())
        goalDao.insertGoals(InitialData.getSampleGoals(todayStr))
        homeworkDao.insertHomeworks(InitialData.getSampleHomeworks())
        examDao.insertExams(InitialData.getSampleExams())
        dhikrDao.insertDhikrs(InitialData.initialDhikrs)
        studyNoteDao.insertStudyNotes(InitialData.initialStudyNotes)
        if (taskDao.getTaskCount() == 0) {
            taskDao.insertTasks(InitialData.getSampleTasks())
        }

        prefs.edit()
            .putBoolean("is_database_seeded", true)
            .putBoolean("is_tasks_seeded", true)
            .putString("last_goal_rollover_date", todayStr)
            .apply()
    }

    private suspend fun handleDailyGoalRollover(todayStr: String, prefs: android.content.SharedPreferences) {
        val lastRollover = prefs.getString("last_goal_rollover_date", "")
        if (lastRollover == todayStr) {
            // Already initialized for today. Any items added, modified, or deleted today stay as is.
            return
        }

        val todayCount = goalDao.getGoalCountForDate(todayStr)
        if (todayCount == 0) {
            val allPastGoals = goalDao.getAllGoalsList()
            if (allPastGoals.isNotEmpty()) {
                val distinctGoals = allPastGoals.distinctBy { it.title.trim().lowercase(Locale.ROOT) }
                val newGoalsForToday = distinctGoals.map { goal ->
                    DailyGoalEntity(
                        title = goal.title,
                        category = goal.category,
                        targetCount = goal.targetCount,
                        currentCount = 0,
                        unit = goal.unit,
                        isCompleted = false,
                        dateString = todayStr
                    )
                }
                goalDao.insertGoals(newGoalsForToday)
            }
        }

        prefs.edit().putString("last_goal_rollover_date", todayStr).apply()
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

    // --- DHIKR / ZİKİRMATİK (DhikrDao) ---
    val allDhikrs: Flow<List<DhikrEntity>> = dhikrDao.getAllDhikrs()

    suspend fun incrementDhikr(dhikr: DhikrEntity) = withContext(Dispatchers.IO) {
        val nextCount = dhikr.currentCount + 1
        dhikrDao.updateDhikr(
            dhikr.copy(
                currentCount = nextCount,
                totalLifetimeCount = dhikr.totalLifetimeCount + 1
            )
        )
    }

    suspend fun resetDhikrRound(dhikr: DhikrEntity) = withContext(Dispatchers.IO) {
        dhikrDao.updateDhikr(dhikr.copy(currentCount = 0))
    }

    suspend fun addDhikr(title: String, meaning: String, targetCount: Int) = withContext(Dispatchers.IO) {
        dhikrDao.insertDhikr(
            DhikrEntity(
                title = title.trim(),
                arabicOrMeaning = meaning.trim(),
                currentCount = 0,
                targetCount = targetCount.coerceAtLeast(1),
                totalLifetimeCount = 0
            )
        )
    }

    suspend fun deleteDhikr(dhikr: DhikrEntity) = withContext(Dispatchers.IO) {
        dhikrDao.deleteDhikr(dhikr)
    }

    // --- STUDY NOTES / EZBER KARTLARI (StudyNoteDao) ---
    val allStudyNotes: Flow<List<StudyNoteEntity>> = studyNoteDao.getAllStudyNotes()

    suspend fun addStudyNote(courseName: String, title: String, content: String) = withContext(Dispatchers.IO) {
        studyNoteDao.insertStudyNote(
            StudyNoteEntity(
                courseName = courseName.trim(),
                title = title.trim(),
                content = content.trim()
            )
        )
    }

    suspend fun deleteStudyNote(note: StudyNoteEntity) = withContext(Dispatchers.IO) {
        studyNoteDao.deleteStudyNote(note)
    }

    // --- STUDY SESSIONS / POMODORO (StudySessionDao) ---
    val allStudySessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllStudySessions()

    fun getTodayStudyMinutes(): Flow<Int> =
        studySessionDao.getTotalMinutesForDate(getTodayDateString())

    val totalLifetimeStudyMinutes: Flow<Int> =
        studySessionDao.getTotalLifetimeMinutes()

    suspend fun recordStudySession(courseOrTopic: String, durationMinutes: Int) = withContext(Dispatchers.IO) {
        studySessionDao.insertStudySession(
            StudySessionEntity(
                courseOrTopic = courseOrTopic.trim().ifBlank { "Genel Ders Çalışma" },
                durationMinutes = durationMinutes.coerceAtLeast(1),
                dateString = getTodayDateString()
            )
        )
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
        goalDao.deleteGoalsByTitle(goal.title)
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

    // --- TASKS & MOTIVATION (TaskDao) ---
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    suspend fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        dueDateMillis: Long?,
        hasReminder: Boolean,
        recurrence: TaskRecurrence,
        subtasks: List<SubtaskItem>,
        tags: String
    ): Long = withContext(Dispatchers.IO) {
        val cleanTitle = title.trim()
        val encodedSubtasks = TaskEntity.encodeSubtasks(subtasks)
        val task = TaskEntity(
            title = cleanTitle,
            description = description.trim(),
            category = category,
            priority = priority,
            dueDateMillis = dueDateMillis,
            hasReminder = hasReminder && dueDateMillis != null,
            recurrence = recurrence,
            subtasksRaw = encodedSubtasks,
            tags = tags.trim(),
            focusedMinutes = 0,
            isCompleted = false
        )
        val id = taskDao.insertTask(task)
        if (hasReminder && dueDateMillis != null && dueDateMillis > System.currentTimeMillis()) {
            NotificationHelper.scheduleTaskReminder(context, id, dueDateMillis)
        }
        id
    }

    suspend fun toggleTaskCompleted(task: TaskEntity): Boolean = withContext(Dispatchers.IO) {
        val nowCompleted = !task.isCompleted
        val now = System.currentTimeMillis()

        val currentSubtasks = task.parseSubtasks()
        val updatedSubtasksRaw = if (nowCompleted && currentSubtasks.isNotEmpty()) {
            TaskEntity.encodeSubtasks(currentSubtasks.map { it.copy(isDone = true) })
        } else {
            task.subtasksRaw
        }

        val updatedTask = task.copy(
            isCompleted = nowCompleted,
            completedAt = if (nowCompleted) now else null,
            subtasksRaw = updatedSubtasksRaw
        )
        taskDao.updateTask(updatedTask)

        if (nowCompleted) {
            NotificationHelper.cancelTaskReminder(context, task.id)
            if (task.recurrence != TaskRecurrence.NONE) {
                val baseTime = task.dueDateMillis ?: now
                val nextDueMillis = when (task.recurrence) {
                    TaskRecurrence.DAILY -> baseTime + 24L * 3600L * 1000L
                    TaskRecurrence.WEEKLY -> baseTime + 7L * 24L * 3600L * 1000L
                    TaskRecurrence.NONE -> null
                }
                val resetSubtasks = TaskEntity.encodeSubtasks(currentSubtasks.map { it.copy(isDone = false) })
                val nextTask = TaskEntity(
                    title = task.title,
                    description = task.description,
                    category = task.category,
                    priority = task.priority,
                    dueDateMillis = nextDueMillis,
                    hasReminder = task.hasReminder && nextDueMillis != null,
                    recurrence = task.recurrence,
                    subtasksRaw = resetSubtasks,
                    tags = task.tags,
                    focusedMinutes = 0,
                    isCompleted = false
                )
                val nextId = taskDao.insertTask(nextTask)
                if (nextTask.hasReminder && nextDueMillis != null && nextDueMillis > now) {
                    NotificationHelper.scheduleTaskReminder(context, nextId, nextDueMillis)
                }
            }
        } else {
            val due = task.dueDateMillis
            if (task.hasReminder && due != null && due > now) {
                NotificationHelper.scheduleTaskReminder(context, task.id, due)
            }
        }
        nowCompleted
    }

    suspend fun toggleSubtask(task: TaskEntity, subtaskIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val list = task.parseSubtasks().toMutableList()
        if (subtaskIndex !in list.indices) return@withContext false

        val item = list[subtaskIndex]
        list[subtaskIndex] = item.copy(isDone = !item.isDone)
        val allDone = list.isNotEmpty() && list.all { it.isDone }
        val encoded = TaskEntity.encodeSubtasks(list)

        if (allDone && !task.isCompleted) {
            toggleTaskCompleted(task.copy(subtasksRaw = encoded))
            return@withContext true
        } else {
            val updated = task.copy(
                subtasksRaw = encoded,
                isCompleted = if (!allDone && task.isCompleted) false else task.isCompleted,
                completedAt = if (!allDone && task.isCompleted) null else task.completedAt
            )
            taskDao.updateTask(updated)
            return@withContext false
        }
    }

    suspend fun addFocusMinutesToTask(taskId: Long, minutes: Int) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        taskDao.updateTask(task.copy(focusedMinutes = task.focusedMinutes + minutes.coerceAtLeast(1)))
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelTaskReminder(context, task.id)
        taskDao.deleteTask(task)
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
