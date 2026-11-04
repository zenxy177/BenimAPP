package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        QuoteEntity::class,
        HomeworkEntity::class,
        ExamEntity::class,
        ScheduleLessonEntity::class,
        DailyGoalEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

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
                    .fallbackToDestructiveMigration()
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
                val dao = database.appDao()
                if (dao.getQuoteCount() == 0) {
                    dao.insertQuotes(InitialData.initialQuotes)
                }
                if (dao.getScheduleCount() == 0) {
                    dao.insertLessons(InitialData.getSampleSchedule())
                }
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (dao.getGoalCountForDate(todayStr) == 0) {
                    dao.insertGoals(InitialData.getSampleGoals(todayStr))
                }
                InitialData.getSampleHomeworks().forEach { dao.insertHomework(it) }
                InitialData.getSampleExams().forEach { dao.insertExam(it) }
            }
        }
    }
}
