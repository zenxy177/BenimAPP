package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.TaskCategory
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE priority WHEN 'YUKSEK' THEN 1 WHEN 'ORTA' THEN 2 ELSE 3 END ASC, COALESCE(dueDateMillis, 9223372036854775807) ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY CASE priority WHEN 'YUKSEK' THEN 1 WHEN 'ORTA' THEN 2 ELSE 3 END ASC, COALESCE(dueDateMillis, 9223372036854775807) ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isCompleted ASC, id DESC")
    fun getTasksByCategory(category: TaskCategory): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE hasReminder = 1 AND isCompleted = 0")
    suspend fun getActiveReminderTasks(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
