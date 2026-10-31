package com.example.gpstracker.core.data.saved

import kotlinx.coroutines.flow.Flow

interface SavedPlacesRepository {
    fun observePlaces(): Flow<List<SavedPlace>>

    fun observeCollections(): Flow<List<PlaceCollection>>

    fun observePlace(id: Long): Flow<SavedPlace?>

    fun observeIsSaved(placeId: String?, latitude: Double, longitude: Double): Flow<Boolean>

    suspend fun getPlace(id: Long): SavedPlace?

    /**
     * Adds the place if not saved; removes it if already saved (dedupe rules).
     */
    suspend fun toggleSaved(request: SavePlaceRequest): ToggleSavedResult

    suspend fun save(request: SavePlaceRequest): SavedPlace

    suspend fun remove(id: Long): SavedPlace?

    /** Re-inserts a previously removed place (Undo). */
    suspend fun restore(place: SavedPlace): SavedPlace

    suspend fun createCollection(name: String): PlaceCollection
}
