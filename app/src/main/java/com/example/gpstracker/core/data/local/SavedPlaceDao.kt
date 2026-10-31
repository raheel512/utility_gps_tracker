package com.example.gpstracker.core.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<SavedPlaceEntity>

    @Query("SELECT * FROM saved_places WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedPlaceEntity?

    @Query("SELECT * FROM saved_places WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SavedPlaceEntity?>

    @Query("SELECT * FROM saved_places WHERE placeId = :placeId LIMIT 1")
    suspend fun findByPlaceId(placeId: String): SavedPlaceEntity?

    @Query(
        "SELECT * FROM saved_places WHERE placeId IS NULL AND latKey = :latKey AND lngKey = :lngKey LIMIT 1",
    )
    suspend fun findByCoordinateKeys(latKey: Long, lngKey: Long): SavedPlaceEntity?

    @Query("SELECT COUNT(*) FROM saved_places WHERE placeId = :placeId")
    fun observeSavedByPlaceId(placeId: String): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM saved_places WHERE placeId IS NULL AND latKey = :latKey AND lngKey = :lngKey",
    )
    fun observeSavedByCoordinateKeys(latKey: Long, lngKey: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM saved_places WHERE collectionId = :collectionId")
    suspend fun countInCollection(collectionId: Long): Int

    @Insert
    suspend fun insert(entity: SavedPlaceEntity): Long

    @Update
    suspend fun update(entity: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteById(id: Long)
}
