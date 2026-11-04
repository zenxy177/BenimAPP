package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.HomeworkEntity
import com.example.data.local.entity.HomeworkType
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {

    @Query("SELECT * FROM homeworks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllHomeworks(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homeworks WHERE type = :type ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getHomeworksByType(type: HomeworkType): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homeworks WHERE isCompleted = 0 ORDER BY dueDateMillis ASC")
    fun getPendingHomeworks(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homeworks WHERE id = :id LIMIT 1")
    suspend fun getHomeworkById(id: Long): HomeworkEntity?

    @Query("SELECT COUNT(*) FROM homeworks")
    suspend fun getHomeworkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworks(homeworks: List<HomeworkEntity>)

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Delete
    suspend fun deleteHomework(homework: HomeworkEntity)

    @Query("DELETE FROM homeworks WHERE id = :id")
    suspend fun deleteHomeworkById(id: Long)
}
