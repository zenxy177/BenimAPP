package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ScheduleLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

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
}
