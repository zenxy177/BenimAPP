package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams ORDER BY examDateMillis ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE examDateMillis >= :currentTimeMillis ORDER BY examDateMillis ASC")
    fun getUpcomingExams(currentTimeMillis: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE courseName LIKE '%' || :course || '%' ORDER BY examDateMillis ASC")
    fun getExamsByCourse(course: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): ExamEntity?

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun getExamCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)
}
