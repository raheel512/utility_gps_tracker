package com.example.gpstracker.core.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class FakeLocationRepository(
    var currentLocation: UserLocation? = null,
) : LocationRepository {

    private val updates = MutableSharedFlow<UserLocation>(extraBufferCapacity = 16)

    var activeCollectors = 0
        private set

    override suspend fun getCurrentLocation(timeoutMs: Long): UserLocation? = currentLocation

    override fun locationUpdates(intervalMs: Long): Flow<UserLocation> = updates
        .onStart { activeCollectors++ }
        .onCompletion { activeCollectors-- }

    suspend fun emit(location: UserLocation) {
        updates.emit(location)
    }
}

fun userLocation(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float = 5f,
) = UserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    speedMps = null,
    altitudeMeters = null,
    bearingDegrees = null,
    timestampMillis = 0L,
)
