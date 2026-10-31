package com.example.gpstracker.core.data.history

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.local.RouteHistoryDao
import com.example.gpstracker.core.data.local.RouteHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRouteHistoryRepository @Inject constructor(
    private val dao: RouteHistoryDao,
    private val dispatchers: DispatcherProvider,
) : RouteHistoryRepository {

    override fun observeAll(): Flow<List<RouteHistoryEntry>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun record(entry: NewRouteHistory): RouteHistoryEntry =
        withContext(dispatchers.io) {
            val entity = entry.toEntity()
            val id = dao.insert(entity)
            entity.copy(id = id).toDomain()
        }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) {
        dao.deleteById(id)
    }
}

private fun RouteHistoryEntity.toDomain() = RouteHistoryEntry(
    id = id,
    originLabel = originLabel,
    destLabel = destLabel,
    destPlaceId = destPlaceId,
    destLatitude = destLatitude,
    destLongitude = destLongitude,
    viaLabel = viaLabel,
    viaPlaceId = viaPlaceId,
    viaLatitude = viaLatitude,
    viaLongitude = viaLongitude,
    plannedDurationSeconds = plannedDurationSeconds,
    plannedDistanceMeters = plannedDistanceMeters,
    encodedPolyline = encodedPolyline,
    startedAtMillis = startedAtMillis,
    hasRouteStats = hasRouteStats,
)

private fun NewRouteHistory.toEntity() = RouteHistoryEntity(
    originLabel = originLabel,
    destLabel = destLabel,
    destPlaceId = destPlaceId,
    destLatitude = destLatitude,
    destLongitude = destLongitude,
    viaLabel = viaLabel,
    viaPlaceId = viaPlaceId,
    viaLatitude = viaLatitude,
    viaLongitude = viaLongitude,
    plannedDurationSeconds = plannedDurationSeconds,
    plannedDistanceMeters = plannedDistanceMeters,
    encodedPolyline = encodedPolyline,
    startedAtMillis = startedAtMillis,
    hasRouteStats = hasRouteStats,
)
