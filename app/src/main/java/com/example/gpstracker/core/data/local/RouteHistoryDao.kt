package com.example.gpstracker.core.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteHistoryDao {
    @Query("SELECT * FROM route_history ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<RouteHistoryEntity>>

    @Query("SELECT * FROM route_history ORDER BY startedAtMillis DESC")
    suspend fun getAll(): List<RouteHistoryEntity>

    @Query("SELECT * FROM route_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RouteHistoryEntity?

    @Insert
    suspend fun insert(entity: RouteHistoryEntity): Long

    @Query("DELETE FROM route_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
