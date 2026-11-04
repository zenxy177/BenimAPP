package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        HomeworkEntity::class,
        ExamEntity::class,
        DailyGoalEntity::class,
        QuoteEntity::class,
        ScheduleLessonEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun homeworkDao(): HomeworkDao
    abstract fun examDao(): ExamDao
    abstract fun goalDao(): GoalDao
    abstract fun quoteDao(): QuoteDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rehberim_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val quoteDao = database.quoteDao()
                val homeworkDao = database.homeworkDao()
                val examDao = database.examDao()
                val goalDao = database.goalDao()
                val scheduleDao = database.scheduleDao()

                if (quoteDao.getQuoteCount() == 0) {
                    quoteDao.insertQuotes(InitialData.initialQuotes)
                }
                if (scheduleDao.getScheduleCount() == 0) {
                    scheduleDao.insertLessons(InitialData.getSampleSchedule())
                }
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
        }
    }
}
