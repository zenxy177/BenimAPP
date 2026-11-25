package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InitialData
import com.example.data.local.entity.*
import com.example.data.repository.AppRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class MainTab {
    HOME,
    QUOTES,
    SCHOOL,
    TIMETABLE,
    GOALS
}

data class GoalsProgressSummary(
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressFraction: Float = 0f,
    val progressPercent: Int = 0
)

data class GradeAverageSummary(
    val averageScore: Float = 0f,
    val gradedExamsCount: Int = 0,
    val totalExamsCount: Int = 0,
    val certificateStatus: String = "Henüz Not Girilmedi",
    val pointsToNextTier: Float = 0f
)

data class BadgeInfo(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val isUnlocked: Boolean
)

data class MotivationState(
    val currentStreakDays: Int = 1,
    val bestStreakDays: Int = 1,
    val totalXp: Int = 30,
    val completedTasksCount: Int = 0,
    val levelNumber: Int = 1,
    val levelTitle: String = "Gayretli Talebe 🌱",
    val currentLevelXp: Int = 30,
    val nextLevelXpTarget: Int = 100,
    val levelProgressFraction: Float = 0.3f,
    val badges: List<BadgeInfo> = emptyList()
)

data class TaskCelebrationEvent(
    val taskTitle: String,
    val earnedXp: Int,
    val streakDays: Int,
    val quoteText: String,
    val quoteSource: String,
    val encouragementNote: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefs = appContext.getSharedPreferences("rehberim_prefs", Context.MODE_PRIVATE)
    private val repository = AppRepository(appContext)

    // Siyah Mod (True Black Mode) State — Defaults to true so Siyah Mod is active out-of-the-box
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit().putBoolean("is_dark_mode", next).apply()
            _uiMessage.emit(if (next) "Siyah Mod etkinleştirildi 🌙" else "Açık Mod etkinleştirildi ☀️")
        }
    }

    fun setDarkMode(enabled: Boolean) {
        if (_isDarkMode.value != enabled) {
            _isDarkMode.value = enabled
            viewModelScope.launch(Dispatchers.IO) {
                prefs.edit().putBoolean("is_dark_mode", enabled).apply()
                _uiMessage.emit(if (enabled) "Siyah Mod etkinleştirildi 🌙" else "Açık Mod etkinleştirildi ☀️")
            }
        }
    }

    // Current navigation tab
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    fun selectTab(tab: MainTab) {
        if (_currentTab.value != tab) {
            _currentTab.value = tab
        }
    }

    // --- QUOTES STATE & BACKGROUND FILTERING ---
    val allQuotes: StateFlow<List<QuoteEntity>> = repository.allQuotes
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _featuredQuote = MutableStateFlow<QuoteEntity?>(null)
    val featuredQuote: StateFlow<QuoteEntity?> = _featuredQuote.asStateFlow()

    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(null)
    val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory.asStateFlow()

    private val _quoteSearchQuery = MutableStateFlow("")
    val quoteSearchQuery: StateFlow<String> = _quoteSearchQuery.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    val filteredQuotes: StateFlow<List<QuoteEntity>> = combine(
        allQuotes,
        _selectedQuoteCategory,
        _quoteSearchQuery,
        _showOnlyFavorites
    ) { quotes, category, query, favOnly ->
        val trimmed = query.trim()
        quotes.filter { quote ->
            val matchesCat = category == null || quote.category == category
            val matchesFav = !favOnly || quote.isFavorite
            val matchesQuery = trimmed.isEmpty() ||
                quote.text.contains(trimmed, ignoreCase = true) ||
                quote.source.contains(trimmed, ignoreCase = true)
            matchesCat && matchesFav && matchesQuery
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DHIKR / ZIKIRMATIK STATE ---
    val allDhikrs: StateFlow<List<DhikrEntity>> = repository.allDhikrs
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationIntervalHours = MutableStateFlow(4)
    val notificationIntervalHours: StateFlow<Int> = _notificationIntervalHours.asStateFlow()

    // --- HOMEWORKS STATE & BACKGROUND FILTERING ---
    val allHomeworks: StateFlow<List<HomeworkEntity>> = repository.allHomeworks
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _homeworkTypeFilter = MutableStateFlow<HomeworkType?>(null)
    val homeworkTypeFilter: StateFlow<HomeworkType?> = _homeworkTypeFilter.asStateFlow()

    val upcomingHomeworks: StateFlow<List<HomeworkEntity>> = allHomeworks
        .map { list -> list.filter { !it.isCompleted }.take(3) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EXAMS & GRADE AVERAGE STATE ---
    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val upcomingExams: StateFlow<List<ExamEntity>> = allExams
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.examDateMillis >= now }.sortedBy { it.examDateMillis }.take(3)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gradeAverageSummary: StateFlow<GradeAverageSummary> = allExams
        .map { exams ->
            val scores = exams.mapNotNull { it.actualGrade ?: it.targetGrade }
            val actualGradedCount = exams.count { it.actualGrade != null }
            if (scores.isEmpty()) {
                GradeAverageSummary(
                    averageScore = 0f,
                    gradedExamsCount = 0,
                    totalExamsCount = exams.size,
                    certificateStatus = "Henüz Not Girilmedi",
                    pointsToNextTier = 70f
                )
            } else {
                val avg = scores.average().toFloat()
                val (status, nextDiff) = when {
                    avg >= 85f -> Pair("Takdir Belgesi 🏆", 0f)
                    avg >= 70f -> Pair("Teşekkür Belgesi 🎖️", 85f - avg)
                    avg >= 50f -> Pair("Doğrudan Geçer ✅", 70f - avg)
                    else -> Pair("Geliştirilmeli 📈", 50f - avg)
                }
                GradeAverageSummary(
                    averageScore = avg,
                    gradedExamsCount = actualGradedCount,
                    totalExamsCount = exams.size,
                    certificateStatus = status,
                    pointsToNextTier = nextDiff.coerceAtLeast(0f)
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GradeAverageSummary())

    // --- STUDY NOTES / FLASHCARDS STATE ---
    val allStudyNotes: StateFlow<List<StudyNoteEntity>> = repository.allStudyNotes
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // --- SCHEDULE STATE & BACKGROUND FILTERING ---
    val allLessons: StateFlow<List<ScheduleLessonEntity>> = repository.allLessons
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val todayDayNumber: Int = getTodayDayOfWeek()

    val todayLessons: StateFlow<List<ScheduleLessonEntity>> = allLessons
        .map { lessons ->
            lessons.filter { it.dayOfWeek == todayDayNumber }.sortedBy { it.periodIndex }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedScheduleDay = MutableStateFlow(todayDayNumber)
    val selectedScheduleDay: StateFlow<Int> = _selectedScheduleDay.asStateFlow()

    val selectedDayLessons: StateFlow<List<ScheduleLessonEntity>> = combine(
        allLessons,
        _selectedScheduleDay
    ) { lessons, day ->
        lessons.filter { it.dayOfWeek == day }.sortedBy { it.periodIndex }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- GOALS STATE & BACKGROUND SUMMARY ---
    val todayGoals: StateFlow<List<DailyGoalEntity>> = repository.getTodayGoals()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val goalsProgressSummary: StateFlow<GoalsProgressSummary> = todayGoals
        .map { goals ->
            val total = goals.size
            val done = goals.count { it.isCompleted }
            val fraction = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
            GoalsProgressSummary(
                completedCount = done,
                totalCount = total,
                progressFraction = fraction,
                progressPercent = (fraction * 100).toInt()
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsProgressSummary())

    // --- TASKS & MOTIVATION STATE ---
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.pendingTasks
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTaskCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedTaskCategory: StateFlow<TaskCategory?> = _selectedTaskCategory.asStateFlow()

    val urgentOrOverdueTask: StateFlow<TaskEntity?> = pendingTasks
        .map { tasks ->
            val now = System.currentTimeMillis()
            tasks.firstOrNull { it.dueDateMillis != null && it.dueDateMillis < now }
                ?: tasks.firstOrNull { it.priority == Priority.YUKSEK }
                ?: tasks.firstOrNull()
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _motivationState = MutableStateFlow(loadMotivationStateFromPrefs())
    val motivationState: StateFlow<MotivationState> = _motivationState.asStateFlow()

    private val _celebrationEvent = MutableStateFlow<TaskCelebrationEvent?>(null)
    val celebrationEvent: StateFlow<TaskCelebrationEvent?> = _celebrationEvent.asStateFlow()

    fun dismissCelebrationEvent() {
        _celebrationEvent.value = null
    }

    // --- STUDY SESSIONS & POMODORO TIMER STATE ---
    val todayStudyMinutes: StateFlow<Int> = repository.getTodayStudyMinutes()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLifetimeStudyMinutes: StateFlow<Int> = repository.totalLifetimeStudyMinutes
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allStudySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pomodoroSelectedMinutes = MutableStateFlow(25)
    val pomodoroSelectedMinutes: StateFlow<Int> = _pomodoroSelectedMinutes.asStateFlow()

    private val _pomodoroRemainingSeconds = MutableStateFlow(25 * 60)
    val pomodoroRemainingSeconds: StateFlow<Int> = _pomodoroRemainingSeconds.asStateFlow()

    private val _pomodoroIsRunning = MutableStateFlow(false)
    val pomodoroIsRunning: StateFlow<Boolean> = _pomodoroIsRunning.asStateFlow()

    private val _pomodoroCourse = MutableStateFlow("Genel Çalışma")
    val pomodoroCourse: StateFlow<String> = _pomodoroCourse.asStateFlow()

    private val _activePomodoroTaskId = MutableStateFlow<Long?>(null)
    val activePomodoroTaskId: StateFlow<Long?> = _activePomodoroTaskId.asStateFlow()

    private var pomodoroJob: Job? = null

    fun setPomodoroDuration(minutes: Int) {
        pomodoroJob?.cancel()
        _pomodoroIsRunning.value = false
        _pomodoroSelectedMinutes.value = minutes
        _pomodoroRemainingSeconds.value = minutes * 60
    }

    fun setPomodoroCourse(course: String) {
        _pomodoroCourse.value = course.ifBlank { "Genel Çalışma" }
        _activePomodoroTaskId.value = null
    }

    fun startFocusOnTask(task: TaskEntity, durationMinutes: Int = 25) {
        pomodoroJob?.cancel()
        _activePomodoroTaskId.value = task.id
        _pomodoroCourse.value = "Görev: ${task.title}"
        _pomodoroSelectedMinutes.value = durationMinutes
        _pomodoroRemainingSeconds.value = durationMinutes * 60
        _currentTab.value = MainTab.HOME
        startPausePomodoro()
        viewModelScope.launch {
            _uiMessage.emit("⏱️ '${task.title}' için odaklanma sayacı başlatıldı!")
        }
    }

    fun startPausePomodoro() {
        if (_pomodoroIsRunning.value) {
            pomodoroJob?.cancel()
            _pomodoroIsRunning.value = false
        } else {
            _pomodoroIsRunning.value = true
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroRemainingSeconds.value > 0 && _pomodoroIsRunning.value) {
                    delay(1000L)
                    _pomodoroRemainingSeconds.value -= 1
                }
                if (_pomodoroRemainingSeconds.value <= 0 && _pomodoroIsRunning.value) {
                    _pomodoroIsRunning.value = false
                    val completedMins = _pomodoroSelectedMinutes.value
                    val subject = _pomodoroCourse.value
                    val linkedTaskId = _activePomodoroTaskId.value
                    _pomodoroRemainingSeconds.value = completedMins * 60
                    launch(Dispatchers.IO) {
                        repository.recordStudySession(subject, completedMins)
                        if (linkedTaskId != null) {
                            repository.addFocusMinutesToTask(linkedTaskId, completedMins)
                        }
                        awardMotivationProgress(xpDelta = 15, incrementTaskCount = false)
                        NotificationHelper.showReminderNotification(
                            appContext,
                            "Pomodoro Tamamlandı! ⏱️ (+15 XP)",
                            "$subject için $completedMins dakikalık odaklanma oturumunu başarıyla tamamladın!",
                            9901L
                        )
                        _uiMessage.emit("Tebrikler! $completedMins dk odaklanma kaydedildi (+15 Azim Puanı).")
                    }
                }
            }
        }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _pomodoroIsRunning.value = false
        _pomodoroRemainingSeconds.value = _pomodoroSelectedMinutes.value * 60
    }

    // --- REMINDERS STATE ---
    val allReminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // UI feedback toast / snackbar
    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndSeedInitialData()
            val random = repository.getRandomQuote() ?: InitialData.initialQuotes.random()
            _featuredQuote.value = random
            NotificationHelper.schedulePeriodicQuoteNotification(
                getApplication(),
                _notificationIntervalHours.value
            )
        }
    }

    private fun getTodayDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
    }

    fun refreshFeaturedQuote() {
        viewModelScope.launch(Dispatchers.IO) {
            val random = repository.getRandomQuote() ?: InitialData.initialQuotes.random()
            _featuredQuote.value = random
        }
    }

    fun setQuoteCategoryFilter(category: QuoteCategory?) {
        _selectedQuoteCategory.value = category
    }

    fun setQuoteSearchQuery(query: String) {
        _quoteSearchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavoriteQuote(quote: QuoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteQuote(quote)
            if (_featuredQuote.value?.id == quote.id) {
                _featuredQuote.value = _featuredQuote.value?.copy(isFavorite = !quote.isFavorite)
            }
        }
    }

    fun addQuote(text: String, source: String, category: QuoteCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addQuote(text, source, category)
            _uiMessage.emit("Hikmetli söz başarıyla eklendi.")
        }
    }

    fun deleteQuote(quote: QuoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQuote(quote)
            _uiMessage.emit("Söz silindi.")
        }
    }

    // Dhikr / Zikirmatik actions
    fun incrementDhikr(dhikr: DhikrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementDhikr(dhikr)
        }
    }

    fun resetDhikrRound(dhikr: DhikrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetDhikrRound(dhikr)
            _uiMessage.emit("${dhikr.title} tur sayacı sıfırlandı.")
        }
    }

    fun addDhikr(title: String, meaning: String, targetCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addDhikr(title, meaning, targetCount)
            _uiMessage.emit("Yeni tesbihat eklendi.")
        }
    }

    fun deleteDhikr(dhikr: DhikrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDhikr(dhikr)
            _uiMessage.emit("Tesbihat silindi.")
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.triggerTestQuoteNotification()
            _uiMessage.emit("İlham bildirimi gönderildi! Bildirim çubuğunu kontrol edebilirsiniz.")
        }
    }

    fun setNotificationInterval(hours: Int) {
        _notificationIntervalHours.value = hours
        if (_notificationsEnabled.value) {
            NotificationHelper.schedulePeriodicQuoteNotification(getApplication(), hours)
        }
        viewModelScope.launch {
            _uiMessage.emit("Bildirim sıklığı: Her $hours saatte bir olarak güncellendi.")
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        if (enabled) {
            NotificationHelper.schedulePeriodicQuoteNotification(
                getApplication(),
                _notificationIntervalHours.value
            )
            viewModelScope.launch { _uiMessage.emit("Hikmet bildirimleri açıldı.") }
        } else {
            NotificationHelper.cancelPeriodicQuoteNotification(getApplication())
            viewModelScope.launch { _uiMessage.emit("Hikmet bildirimleri kapatıldı.") }
        }
    }

    // Homework actions
    fun setHomeworkFilter(type: HomeworkType?) {
        _homeworkTypeFilter.value = type
    }

    fun addHomework(
        title: String,
        course: String,
        type: HomeworkType,
        description: String,
        dueDateMillis: Long,
        priority: Priority
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHomework(title, course, type, description, dueDateMillis, priority)
            _uiMessage.emit("Ödev kaydedildi.")
        }
    }

    fun toggleHomework(homework: HomeworkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleHomework(homework)
        }
    }

    fun deleteHomework(homework: HomeworkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHomework(homework)
            _uiMessage.emit("Ödev silindi.")
        }
    }

    // Exam actions
    fun addExam(
        courseName: String,
        examTitle: String,
        examDateMillis: Long,
        topics: String,
        targetGrade: Int?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExam(courseName, examTitle, examDateMillis, topics, targetGrade)
            _uiMessage.emit("Sınav takvime eklendi.")
        }
    }

    fun updateExamGrade(exam: ExamEntity, actualGrade: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExamGrade(exam, actualGrade)
            _uiMessage.emit("Sınav notu güncellendi.")
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExam(exam)
            _uiMessage.emit("Sınav silindi.")
        }
    }

    // Study Note / Flashcard actions
    fun addStudyNote(courseName: String, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addStudyNote(courseName, title, content)
            _uiMessage.emit("Ezber kartı eklendi.")
        }
    }

    fun deleteStudyNote(note: StudyNoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyNote(note)
            _uiMessage.emit("Ezber kartı silindi.")
        }
    }

    // Schedule actions
    fun selectScheduleDay(day: Int) {
        _selectedScheduleDay.value = day
    }

    fun addLesson(
        dayOfWeek: Int,
        periodIndex: Int,
        lessonName: String,
        teacherOrRoom: String,
        startTime: String,
        endTime: String,
        colorHex: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLesson(dayOfWeek, periodIndex, lessonName, teacherOrRoom, startTime, endTime, colorHex)
            _uiMessage.emit("Ders programa eklendi.")
        }
    }

    fun deleteLesson(lesson: ScheduleLessonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLesson(lesson)
            _uiMessage.emit("Ders silindi.")
        }
    }

    // Goal actions
    fun addGoal(title: String, category: GoalCategory, targetCount: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGoal(title, category, targetCount, unit)
            _uiMessage.emit("Günlük hedef eklendi.")
        }
    }

    fun incrementGoal(goal: DailyGoalEntity, delta: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementGoal(goal, delta)
        }
    }

    fun toggleGoalCompleted(goal: DailyGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val wasCompleted = goal.isCompleted
            repository.toggleGoalCompleted(goal)
            if (!wasCompleted) {
                awardMotivationProgress(xpDelta = 10, incrementTaskCount = false)
            }
        }
    }

    fun deleteGoal(goal: DailyGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goal)
            _uiMessage.emit("Hedef kaldırıldı.")
        }
    }

    // --- TASK & MOTIVATION ACTIONS ---
    fun setTaskCategoryFilter(category: TaskCategory?) {
        _selectedTaskCategory.value = category
    }

    fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        dueDateMillis: Long?,
        hasReminder: Boolean,
        recurrence: TaskRecurrence,
        subtasks: List<SubtaskItem>,
        tags: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDateMillis = dueDateMillis,
                hasReminder = hasReminder,
                recurrence = recurrence,
                subtasks = subtasks,
                tags = tags
            )
            _uiMessage.emit(
                if (hasReminder && dueDateMillis != null) {
                    "Görev ve motivasyonlu hatırlatıcı alarmı kuruldu! ⏰"
                } else {
                    "Yeni görev planlandı! 🎯"
                }
            )
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val nowCompleted = repository.toggleTaskCompleted(task)
            if (nowCompleted) {
                val earnedXp = when (task.priority) {
                    Priority.YUKSEK -> 35
                    Priority.ORTA -> 20
                    Priority.DUSUK -> 10
                }
                val newState = awardMotivationProgress(xpDelta = earnedXp, incrementTaskCount = true)
                val quote = repository.getRandomQuote() ?: InitialData.initialQuotes.random()
                val encouragement = when (task.priority) {
                    Priority.YUKSEK -> "Zor ve yüksek öncelikli bir görevi başarıyla aştın! Azmin önünde hiçbir engel duramaz."
                    Priority.ORTA -> "İstikrarlı adımlarla hedeflerine yaklaşıyorsun. Bereketli olsun!"
                    Priority.DUSUK -> "Küçük adımlar büyük başarıların temelidir. Harika gidiyorsun!"
                }
                _celebrationEvent.value = TaskCelebrationEvent(
                    taskTitle = task.title,
                    earnedXp = earnedXp,
                    streakDays = newState.currentStreakDays,
                    quoteText = quote.text,
                    quoteSource = quote.source,
                    encouragementNote = encouragement
                )
            }
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val completedWholeTask = repository.toggleSubtask(task, subtaskIndex)
            if (completedWholeTask) {
                val earnedXp = when (task.priority) {
                    Priority.YUKSEK -> 35
                    Priority.ORTA -> 20
                    Priority.DUSUK -> 10
                }
                val newState = awardMotivationProgress(xpDelta = earnedXp, incrementTaskCount = true)
                val quote = repository.getRandomQuote() ?: InitialData.initialQuotes.random()
                _celebrationEvent.value = TaskCelebrationEvent(
                    taskTitle = task.title,
                    earnedXp = earnedXp,
                    streakDays = newState.currentStreakDays,
                    quoteText = quote.text,
                    quoteSource = quote.source,
                    encouragementNote = "Tüm alt adımları tek tek tamamlayarak görevi bitirdin! Planlı çalışman meyvesini veriyor."
                )
            } else {
                awardMotivationProgress(xpDelta = 5, incrementTaskCount = false)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
            _uiMessage.emit("Görev silindi.")
        }
    }

    private fun loadMotivationStateFromPrefs(): MotivationState {
        val streak = prefs.getInt("motivation_streak_days", 1).coerceAtLeast(1)
        val bestStreak = prefs.getInt("motivation_best_streak", streak).coerceAtLeast(streak)
        val xp = prefs.getInt("motivation_total_xp", 40).coerceAtLeast(0)
        val completedCount = prefs.getInt("motivation_completed_tasks", 1).coerceAtLeast(0)
        return buildMotivationState(streak, bestStreak, xp, completedCount)
    }

    private fun awardMotivationProgress(xpDelta: Int, incrementTaskCount: Boolean): MotivationState {
        val todayStr = repository.getTodayDateString()
        val lastActiveDate = prefs.getString("motivation_last_active_date", "") ?: ""
        var currentStreak = prefs.getInt("motivation_streak_days", 1).coerceAtLeast(1)
        var bestStreak = prefs.getInt("motivation_best_streak", currentStreak).coerceAtLeast(currentStreak)

        if (lastActiveDate != todayStr) {
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(yesterdayCal.time)
            currentStreak = if (lastActiveDate == yesterdayStr) {
                currentStreak + 1
            } else if (lastActiveDate.isEmpty()) {
                currentStreak
            } else {
                1
            }
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak
            }
        }

        val newXp = (prefs.getInt("motivation_total_xp", 40) + xpDelta).coerceAtLeast(0)
        val prevCompleted = prefs.getInt("motivation_completed_tasks", 1)
        val newCompleted = if (incrementTaskCount) prevCompleted + 1 else prevCompleted

        prefs.edit()
            .putInt("motivation_streak_days", currentStreak)
            .putInt("motivation_best_streak", bestStreak)
            .putInt("motivation_total_xp", newXp)
            .putInt("motivation_completed_tasks", newCompleted)
            .putString("motivation_last_active_date", todayStr)
            .apply()

        val updated = buildMotivationState(currentStreak, bestStreak, newXp, newCompleted)
        _motivationState.value = updated
        return updated
    }

    private fun buildMotivationState(
        streak: Int,
        bestStreak: Int,
        xp: Int,
        completedTasks: Int
    ): MotivationState {
        val (levelNum, levelTitle, minXp, maxXp) = when {
            xp >= 600 -> Quadruple(5, "Hikmet ve İlim Rehberi 👑", 600, 1000)
            xp >= 350 -> Quadruple(4, "İstikrar Öncüsü 🏅", 350, 600)
            xp >= 180 -> Quadruple(3, "Azimli Yolcu 🔥", 180, 350)
            xp >= 80 -> Quadruple(2, "Planlı Öğrenci ⭐", 80, 180)
            else -> Quadruple(1, "Gayretli Talebe 🌱", 0, 80)
        }
        val span = (maxXp - minXp).coerceAtLeast(1)
        val progressInLevel = (xp - minXp).coerceIn(0, span)
        val fraction = (progressInLevel.toFloat() / span.toFloat()).coerceIn(0f, 1f)

        val badges = listOf(
            BadgeInfo(
                id = "first_step",
                title = "İlk Adım",
                emoji = "🌱",
                description = "İlk görevini başarıyla tamamladın",
                isUnlocked = completedTasks >= 1
            ),
            BadgeInfo(
                id = "streak_3",
                title = "3 Gün Seri",
                emoji = "🔥",
                description = "3 gün üst üste görev tamamladın",
                isUnlocked = bestStreak >= 3
            ),
            BadgeInfo(
                id = "task_master",
                title = "Görev Ustası",
                emoji = "🎯",
                description = "5 veya daha fazla görev bitirdin",
                isUnlocked = completedTasks >= 5
            ),
            BadgeInfo(
                id = "wisdom_seeker",
                title = "Azim Ehli",
                emoji = "🏆",
                description = "200 Azim Puanına (XP) ulaştın",
                isUnlocked = xp >= 200
            )
        )

        return MotivationState(
            currentStreakDays = streak,
            bestStreakDays = bestStreak,
            totalXp = xp,
            completedTasksCount = completedTasks,
            levelNumber = levelNum,
            levelTitle = levelTitle,
            currentLevelXp = xp,
            nextLevelXpTarget = maxXp,
            levelProgressFraction = fraction,
            badges = badges
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // Reminder actions
    fun addReminder(title: String, note: String, triggerTimeMillis: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addReminder(title, note, triggerTimeMillis)
            _uiMessage.emit("Hatırlatıcı kuruldu.")
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleReminder(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(reminder)
            _uiMessage.emit("Hatırlatıcı silindi.")
        }
    }
}
