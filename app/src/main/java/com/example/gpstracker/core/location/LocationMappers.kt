package com.example.gpstracker.core.location

import android.location.Location

internal fun Location.toUserLocation(): UserLocation = mapToUserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    speedMps = if (hasSpeed()) speed else null,
    altitudeMeters = if (hasAltitude()) altitude else null,
    bearingDegrees = if (hasBearing()) bearing else null,
    timestampMillis = time,
)

/**
 * Pure mapping for unit tests (keeps android.location.Location out of test JVM).
 */
fun mapToUserLocation(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float,
    speedMps: Float?,
    altitudeMeters: Double?,
    bearingDegrees: Float?,
    timestampMillis: Long,
): UserLocation = UserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    speedMps = speedMps,
    altitudeMeters = altitudeMeters,
    bearingDegrees = bearingDegrees,
    timestampMillis = timestampMillis,
)
