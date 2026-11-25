package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY completedAt DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateString = :dateStr ORDER BY completedAt DESC")
    fun getSessionsForDate(dateStr: String): Flow<List<StudySessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE dateString = :dateStr")
    fun getTotalMinutesForDate(dateStr: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions")
    fun getTotalLifetimeMinutes(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)
}
