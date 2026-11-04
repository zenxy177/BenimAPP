package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DailyGoalEntity
import com.example.data.local.entity.GoalCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM daily_goals WHERE dateString = :dateStr ORDER BY isCompleted ASC, id ASC")
    fun getGoalsForDate(dateStr: String): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals WHERE dateString = :dateStr AND category = :category ORDER BY isCompleted ASC, id ASC")
    fun getGoalsByDateAndCategory(dateStr: String, category: GoalCategory): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): DailyGoalEntity?

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

    @Query("DELETE FROM daily_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
}
