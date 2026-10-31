package com.example.gpstracker.core.location

/**
 * App-domain location fix. Keep [android.location.Location] out of ViewModels.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float?,
    val altitudeMeters: Double?,
    val bearingDegrees: Float?,
    val timestampMillis: Long,
)
