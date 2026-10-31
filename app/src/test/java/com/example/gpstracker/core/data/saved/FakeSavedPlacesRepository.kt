package com.example.gpstracker.core.data.saved

import com.example.gpstracker.core.data.local.coordinateKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSavedPlacesRepository : SavedPlacesRepository {
    private val places = MutableStateFlow<List<SavedPlace>>(emptyList())
    private val collections = MutableStateFlow<List<PlaceCollection>>(emptyList())
    private var nextId = 1L
    private var nextCollectionId = 1L

    override fun observePlaces(): Flow<List<SavedPlace>> = places

    override fun observeCollections(): Flow<List<PlaceCollection>> = collections

    override fun observePlace(id: Long): Flow<SavedPlace?> =
        places.map { list -> list.firstOrNull { it.id == id } }

    override fun observeIsSaved(
        placeId: String?,
        latitude: Double,
        longitude: Double,
    ): Flow<Boolean> = places.map { list ->
        findExisting(list, placeId, latitude, longitude) != null
    }

    override suspend fun getPlace(id: Long): SavedPlace? =
        places.value.firstOrNull { it.id == id }

    override suspend fun toggleSaved(request: SavePlaceRequest): ToggleSavedResult {
        val existing = findExisting(
            places.value,
            request.placeId,
            request.latitude,
            request.longitude,
        )
        return if (existing != null) {
            places.value = places.value.filterNot { it.id == existing.id }
            ToggleSavedResult.Removed(existing)
        } else {
            ToggleSavedResult.Saved(save(request))
        }
    }

    override suspend fun save(request: SavePlaceRequest): SavedPlace {
        val existing = findExisting(
            places.value,
            request.placeId,
            request.latitude,
            request.longitude,
        )
        if (existing != null) {
            val updated = existing.copy(
                name = request.name,
                address = request.address,
                latitude = request.latitude,
                longitude = request.longitude,
                placeId = request.placeId ?: existing.placeId,
                collectionId = request.collectionId ?: existing.collectionId,
                iconKind = request.iconKind,
            )
            places.value = places.value.map { if (it.id == existing.id) updated else it }
            return updated
        }
        val created = SavedPlace(
            id = nextId++,
            placeId = request.placeId,
            name = request.name,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            collectionId = request.collectionId,
            createdAtMillis = System.currentTimeMillis(),
            iconKind = request.iconKind,
        )
        places.value = listOf(created) + places.value
        return created
    }

    override suspend fun remove(id: Long): SavedPlace? {
        val existing = places.value.firstOrNull { it.id == id } ?: return null
        places.value = places.value.filterNot { it.id == id }
        return existing
    }

    override suspend fun restore(place: SavedPlace): SavedPlace {
        val restored = place.copy(id = nextId++)
        places.value = listOf(restored) + places.value
        return restored
    }

    override suspend fun createCollection(name: String): PlaceCollection {
        val created = PlaceCollection(
            id = nextCollectionId++,
            name = name.trim(),
            createdAtMillis = System.currentTimeMillis(),
            placeCount = 0,
        )
        collections.value = listOf(created) + collections.value
        return created
    }

    fun all(): List<SavedPlace> = places.value

    private fun findExisting(
        list: List<SavedPlace>,
        placeId: String?,
        latitude: Double,
        longitude: Double,
    ): SavedPlace? {
        if (!placeId.isNullOrBlank()) {
            list.firstOrNull { it.placeId == placeId }?.let { return it }
        }
        val latKey = coordinateKey(latitude)
        val lngKey = coordinateKey(longitude)
        return list.firstOrNull {
            it.placeId.isNullOrBlank() &&
                coordinateKey(it.latitude) == latKey &&
                coordinateKey(it.longitude) == lngKey
        }
    }
}
