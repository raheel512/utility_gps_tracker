package com.example.gpstracker.core.data.saved

import com.example.gpstracker.BuildConfig
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.local.CollectionDao
import com.example.gpstracker.core.data.local.CollectionEntity
import com.example.gpstracker.core.data.local.SavedPlaceDao
import com.example.gpstracker.core.data.local.SavedPlaceEntity
import com.example.gpstracker.core.data.local.coordinateKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSavedPlacesRepository @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val collectionDao: CollectionDao,
    private val dispatchers: DispatcherProvider,
) : SavedPlacesRepository {

    override fun observePlaces(): Flow<List<SavedPlace>> =
        savedPlaceDao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override fun observeCollections(): Flow<List<PlaceCollection>> =
        combine(
            collectionDao.observeAll(),
            savedPlaceDao.observeAll(),
        ) { collections, places ->
            collections.map { collection ->
                PlaceCollection(
                    id = collection.id,
                    name = collection.name,
                    createdAtMillis = collection.createdAtMillis,
                    placeCount = places.count { it.collectionId == collection.id },
                )
            }
        }.flowOn(dispatchers.io)

    override fun observePlace(id: Long): Flow<SavedPlace?> =
        savedPlaceDao.observeById(id)
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)

    override fun observeIsSaved(
        placeId: String?,
        latitude: Double,
        longitude: Double,
    ): Flow<Boolean> {
        val flow = if (!placeId.isNullOrBlank()) {
            savedPlaceDao.observeSavedByPlaceId(placeId)
        } else {
            savedPlaceDao.observeSavedByCoordinateKeys(
                coordinateKey(latitude),
                coordinateKey(longitude),
            )
        }
        return flow.map { it > 0 }.flowOn(dispatchers.io)
    }

    override suspend fun getPlace(id: Long): SavedPlace? = withContext(dispatchers.io) {
        savedPlaceDao.getById(id)?.toDomain()
    }

    override suspend fun toggleSaved(request: SavePlaceRequest): ToggleSavedResult =
        withContext(dispatchers.io) {
            val existing = findExisting(request.placeId, request.latitude, request.longitude)
            if (existing != null) {
                savedPlaceDao.deleteById(existing.id)
                logDebug("Removed saved place id=${existing.id}")
                ToggleSavedResult.Removed(existing.toDomain())
            } else {
                val saved = insert(request)
                ToggleSavedResult.Saved(saved)
            }
        }

    override suspend fun save(request: SavePlaceRequest): SavedPlace = withContext(dispatchers.io) {
        val existing = findExisting(request.placeId, request.latitude, request.longitude)
        if (existing != null) {
            val updated = existing.copy(
                name = request.name,
                address = request.address,
                latitude = request.latitude,
                longitude = request.longitude,
                latKey = coordinateKey(request.latitude),
                lngKey = coordinateKey(request.longitude),
                collectionId = request.collectionId ?: existing.collectionId,
                iconKind = request.iconKind.toStorage(),
                placeId = request.placeId ?: existing.placeId,
            )
            savedPlaceDao.update(updated)
            logDebug("Updated saved place id=${updated.id}")
            updated.toDomain()
        } else {
            insert(request)
        }
    }

    override suspend fun remove(id: Long): SavedPlace? = withContext(dispatchers.io) {
        val existing = savedPlaceDao.getById(id) ?: return@withContext null
        savedPlaceDao.deleteById(id)
        logDebug("Removed saved place id=$id")
        existing.toDomain()
    }

    override suspend fun restore(place: SavedPlace): SavedPlace = withContext(dispatchers.io) {
        val entity = SavedPlaceEntity(
            id = 0,
            placeId = place.placeId,
            name = place.name,
            address = place.address,
            latitude = place.latitude,
            longitude = place.longitude,
            latKey = coordinateKey(place.latitude),
            lngKey = coordinateKey(place.longitude),
            collectionId = place.collectionId,
            createdAtMillis = place.createdAtMillis,
            iconKind = place.iconKind.toStorage(),
        )
        val id = savedPlaceDao.insert(entity)
        logDebug("Restored saved place id=$id")
        entity.copy(id = id).toDomain()
    }

    override suspend fun createCollection(name: String): PlaceCollection =
        withContext(dispatchers.io) {
            val trimmed = name.trim()
            require(trimmed.isNotEmpty()) { "Collection name required" }
            val entity = CollectionEntity(
                name = trimmed,
                createdAtMillis = System.currentTimeMillis(),
            )
            val id = collectionDao.insert(entity)
            PlaceCollection(
                id = id,
                name = trimmed,
                createdAtMillis = entity.createdAtMillis,
                placeCount = 0,
            )
        }

    private suspend fun insert(request: SavePlaceRequest): SavedPlace {
        val now = System.currentTimeMillis()
        val entity = SavedPlaceEntity(
            placeId = request.placeId?.takeIf { it.isNotBlank() },
            name = request.name,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            latKey = coordinateKey(request.latitude),
            lngKey = coordinateKey(request.longitude),
            collectionId = request.collectionId,
            createdAtMillis = now,
            iconKind = request.iconKind.toStorage(),
        )
        val id = savedPlaceDao.insert(entity)
        logDebug("Saved place id=$id")
        return entity.copy(id = id).toDomain()
    }

    private suspend fun findExisting(
        placeId: String?,
        latitude: Double,
        longitude: Double,
    ): SavedPlaceEntity? {
        if (!placeId.isNullOrBlank()) {
            savedPlaceDao.findByPlaceId(placeId)?.let { return it }
        }
        return savedPlaceDao.findByCoordinateKeys(
            coordinateKey(latitude),
            coordinateKey(longitude),
        )
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Timber.d(message)
        }
    }
}

internal fun SavedPlaceEntity.toDomain() = SavedPlace(
    id = id,
    placeId = placeId,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    collectionId = collectionId,
    createdAtMillis = createdAtMillis,
    iconKind = iconKind.toIconKind(),
)

private fun String.toIconKind(): SavedPlaceIconKind = when (this) {
    SavedPlaceEntity.IconKind.HOME -> SavedPlaceIconKind.HOME
    SavedPlaceEntity.IconKind.WORK -> SavedPlaceIconKind.WORK
    else -> SavedPlaceIconKind.PLACE
}

private fun SavedPlaceIconKind.toStorage(): String = when (this) {
    SavedPlaceIconKind.HOME -> SavedPlaceEntity.IconKind.HOME
    SavedPlaceIconKind.WORK -> SavedPlaceEntity.IconKind.WORK
    SavedPlaceIconKind.PLACE -> SavedPlaceEntity.IconKind.PLACE
}
