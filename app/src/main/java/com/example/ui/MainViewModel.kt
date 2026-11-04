package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InitialData
import com.example.data.local.entity.*
import com.example.data.repository.AppRepository
import com.example.notification.NotificationHelper
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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    // --- QUOTES STATE ---
    val allQuotes = repository.allQuotes.stateIn(
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

    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationIntervalHours = MutableStateFlow(4)
    val notificationIntervalHours: StateFlow<Int> = _notificationIntervalHours.asStateFlow()

    // --- HOMEWORKS STATE ---
    val allHomeworks = repository.allHomeworks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _homeworkTypeFilter = MutableStateFlow<HomeworkType?>(null)
    val homeworkTypeFilter: StateFlow<HomeworkType?> = _homeworkTypeFilter.asStateFlow()

    // --- EXAMS STATE ---
    val allExams = repository.allExams.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // --- SCHEDULE STATE ---
    val allLessons = repository.allLessons.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedScheduleDay = MutableStateFlow(getTodayDayOfWeek())
    val selectedScheduleDay: StateFlow<Int> = _selectedScheduleDay.asStateFlow()

    // --- GOALS STATE ---
    val todayGoals = repository.getTodayGoals().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // --- REMINDERS STATE ---
    val allReminders = repository.allReminders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // UI feedback toast / snackbar
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            refreshFeaturedQuote()
            // Schedule default periodic quote notification
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
            else -> 1 // Default to Pazartesi for Sunday
        }
    }

    fun refreshFeaturedQuote() {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.toggleFavoriteQuote(quote)
            if (_featuredQuote.value?.id == quote.id) {
                _featuredQuote.value = _featuredQuote.value?.copy(isFavorite = !quote.isFavorite)
            }
        }
    }

    fun addQuote(text: String, source: String, category: QuoteCategory) {
        viewModelScope.launch {
            repository.addQuote(text, source, category)
            _uiMessage.emit("Hikmetli söz başarıyla eklendi.")
        }
    }

    fun deleteQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            repository.deleteQuote(quote)
            _uiMessage.emit("Söz silindi.")
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.addHomework(title, course, type, description, dueDateMillis, priority)
            _uiMessage.emit("Ödev kaydedildi.")
        }
    }

    fun toggleHomework(homework: HomeworkEntity) {
        viewModelScope.launch {
            repository.toggleHomework(homework)
        }
    }

    fun deleteHomework(homework: HomeworkEntity) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            repository.addExam(courseName, examTitle, examDateMillis, topics, targetGrade)
            _uiMessage.emit("Sınav takvime eklendi.")
        }
    }

    fun updateExamGrade(exam: ExamEntity, actualGrade: Int?) {
        viewModelScope.launch {
            repository.updateExamGrade(exam, actualGrade)
            _uiMessage.emit("Sınav notu güncellendi.")
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.deleteExam(exam)
            _uiMessage.emit("Sınav silindi.")
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
        viewModelScope.launch {
            repository.addLesson(dayOfWeek, periodIndex, lessonName, teacherOrRoom, startTime, endTime, colorHex)
            _uiMessage.emit("Ders programa eklendi.")
        }
    }

    fun deleteLesson(lesson: ScheduleLessonEntity) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
            _uiMessage.emit("Ders silindi.")
        }
    }

    // Goal actions
    fun addGoal(title: String, category: GoalCategory, targetCount: Int, unit: String) {
        viewModelScope.launch {
            repository.addGoal(title, category, targetCount, unit)
            _uiMessage.emit("Günlük hedef eklendi.")
        }
    }

    fun incrementGoal(goal: DailyGoalEntity, delta: Int = 1) {
        viewModelScope.launch {
            repository.incrementGoal(goal, delta)
        }
    }

    fun toggleGoalCompleted(goal: DailyGoalEntity) {
        viewModelScope.launch {
            repository.toggleGoalCompleted(goal)
        }
    }

    fun deleteGoal(goal: DailyGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _uiMessage.emit("Hedef kaldırıldı.")
        }
    }

    // Reminder actions
    fun addReminder(title: String, note: String, triggerTimeMillis: Long) {
        viewModelScope.launch {
            repository.addReminder(title, note, triggerTimeMillis)
            _uiMessage.emit("Hatırlatıcı kuruldu.")
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.toggleReminder(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _uiMessage.emit("Hatırlatıcı silindi.")
        }
    }
}
