package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- QUOTES ---
    @Query("SELECT * FROM quotes ORDER BY id DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY id DESC")
    fun getQuotesByCategory(category: QuoteCategory): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)


    // --- HOMEWORKS ---
    @Query("SELECT * FROM homeworks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllHomeworks(): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Delete
    suspend fun deleteHomework(homework: HomeworkEntity)


    // --- EXAMS ---
    @Query("SELECT * FROM exams ORDER BY examDateMillis ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)


    // --- SCHEDULE LESSONS ---
    @Query("SELECT * FROM schedule_lessons ORDER BY dayOfWeek ASC, periodIndex ASC")
    fun getAllLessons(): Flow<List<ScheduleLessonEntity>>

    @Query("SELECT * FROM schedule_lessons WHERE dayOfWeek = :dayOfWeek ORDER BY periodIndex ASC")
    fun getLessonsForDay(dayOfWeek: Int): Flow<List<ScheduleLessonEntity>>

    @Query("SELECT COUNT(*) FROM schedule_lessons")
    suspend fun getScheduleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: ScheduleLessonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<ScheduleLessonEntity>)

    @Update
    suspend fun updateLesson(lesson: ScheduleLessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: ScheduleLessonEntity)


    // --- DAILY GOALS ---
    @Query("SELECT * FROM daily_goals WHERE dateString = :dateStr ORDER BY isCompleted ASC, id ASC")
    fun getGoalsForDate(dateStr: String): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<DailyGoalEntity>>

    @Query("SELECT COUNT(*) FROM daily_goals WHERE dateString = :dateStr")
    suspend fun getGoalCountForDate(dateStr: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoalEntity>)

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: DailyGoalEntity)


    // --- REMINDERS ---
    @Query("SELECT * FROM reminders ORDER BY triggerTimeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}
