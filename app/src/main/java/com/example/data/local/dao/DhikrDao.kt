package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DhikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikr_items ORDER BY id ASC")
    fun getAllDhikrs(): Flow<List<DhikrEntity>>

    @Query("SELECT COUNT(*) FROM dhikr_items")
    suspend fun getDhikrCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(dhikr: DhikrEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrs(dhikrs: List<DhikrEntity>)

    @Update
    suspend fun updateDhikr(dhikr: DhikrEntity)

    @Delete
    suspend fun deleteDhikr(dhikr: DhikrEntity)
}
