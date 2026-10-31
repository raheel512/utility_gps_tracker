package com.example.gpstracker.core.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY updatedAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = MAX_RECENT): Flow<List<RecentSearchEntity>>

    @Query("SELECT * FROM recent_searches ORDER BY updatedAtMillis DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = MAX_RECENT): List<RecentSearchEntity>

    @Upsert
    suspend fun upsert(entity: RecentSearchEntity)

    @Query(
        """
        DELETE FROM recent_searches WHERE placeId NOT IN (
            SELECT placeId FROM recent_searches ORDER BY updatedAtMillis DESC LIMIT :keep
        )
        """,
    )
    suspend fun trimTo(keep: Int = MAX_RECENT)

    companion object {
        const val MAX_RECENT = 10
    }
}
