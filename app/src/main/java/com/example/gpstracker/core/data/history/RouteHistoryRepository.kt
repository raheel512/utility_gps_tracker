package com.example.gpstracker.core.data.history

import kotlinx.coroutines.flow.Flow

interface RouteHistoryRepository {
    fun observeAll(): Flow<List<RouteHistoryEntry>>
    suspend fun record(entry: NewRouteHistory): RouteHistoryEntry
    suspend fun delete(id: Long)
}
