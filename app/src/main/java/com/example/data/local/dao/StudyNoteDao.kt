package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StudyNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyNoteDao {

    @Query("SELECT * FROM study_notes ORDER BY id DESC")
    fun getAllStudyNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE courseName = :course ORDER BY id DESC")
    fun getStudyNotesByCourse(course: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT COUNT(*) FROM study_notes")
    suspend fun getStudyNoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNotes(notes: List<StudyNoteEntity>)

    @Update
    suspend fun updateStudyNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteStudyNote(note: StudyNoteEntity)
}
