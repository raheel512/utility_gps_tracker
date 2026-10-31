package com.example.gpstracker.core.data.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRouteHistoryRepository : RouteHistoryRepository {
    private val items = MutableStateFlow<List<RouteHistoryEntry>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<RouteHistoryEntry>> = items

    override suspend fun record(entry: NewRouteHistory): RouteHistoryEntry {
        val created = RouteHistoryEntry(
            id = nextId++,
            originLabel = entry.originLabel,
            destLabel = entry.destLabel,
            destPlaceId = entry.destPlaceId,
            destLatitude = entry.destLatitude,
            destLongitude = entry.destLongitude,
            viaLabel = entry.viaLabel,
            viaPlaceId = entry.viaPlaceId,
            viaLatitude = entry.viaLatitude,
            viaLongitude = entry.viaLongitude,
            plannedDurationSeconds = entry.plannedDurationSeconds,
            plannedDistanceMeters = entry.plannedDistanceMeters,
            encodedPolyline = entry.encodedPolyline,
            startedAtMillis = entry.startedAtMillis,
            hasRouteStats = entry.hasRouteStats,
        )
        items.value = listOf(created) + items.value
        return created
    }

    override suspend fun delete(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    fun all(): List<RouteHistoryEntry> = items.value
}
