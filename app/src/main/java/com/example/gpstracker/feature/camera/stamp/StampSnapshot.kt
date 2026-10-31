package com.example.gpstracker.feature.camera.stamp

import com.example.gpstracker.core.data.DistanceUnit
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Raw optional stamp inputs. Nothing invented — absent fields stay absent.
 * Temperature is intentionally not a field (arrives with weather later).
 */
data class StampSnapshot(
    val dateTime: ZonedDateTime,
    val zoneId: ZoneId,
    val addressLine: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val speedMps: Float? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val pressureHpa: Float? = null,
    val headingDegrees: Float? = null,
    val headingCardinal: String? = null,
)

data class StampRenderModel(
    val dateTimeLine: String,
    val gmtBadge: String,
    val addressLine: String?,
    val coordinatesLine: String?,
    val altitudeLine: String?,
    val speedLine: String?,
    val pressureLine: String?,
    val headingLine: String?,
)
