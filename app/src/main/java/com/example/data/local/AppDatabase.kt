package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        HomeworkEntity::class,
        ExamEntity::class,
        DailyGoalEntity::class,
        QuoteEntity::class,
        ScheduleLessonEntity::class,
        ReminderEntity::class,
        DhikrEntity::class,
        StudyNoteEntity::class,
        StudySessionEntity::class,
        TaskEntity::class
    ],
    version = 4,
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
    abstract fun dhikrDao(): DhikrDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun taskDao(): TaskDao
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `dueDateMillis` INTEGER,
                        `hasReminder` INTEGER NOT NULL,
                        `recurrence` TEXT NOT NULL,
                        `subtasksRaw` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `focusedMinutes` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_isCompleted` ON `tasks` (`isCompleted`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_category` ON `tasks` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_priority` ON `tasks` (`priority`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDateMillis` ON `tasks` (`dueDateMillis`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rehberim_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
