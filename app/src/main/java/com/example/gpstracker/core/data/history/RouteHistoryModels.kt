package com.example.gpstracker.core.data.history

data class RouteHistoryEntry(
    val id: Long,
    val originLabel: String,
    val destLabel: String,
    val destPlaceId: String?,
    val destLatitude: Double,
    val destLongitude: Double,
    val viaLabel: String?,
    val viaPlaceId: String?,
    val viaLatitude: Double?,
    val viaLongitude: Double?,
    val plannedDurationSeconds: Long,
    val plannedDistanceMeters: Int,
    val encodedPolyline: String,
    val startedAtMillis: Long,
    val hasRouteStats: Boolean = true,
)

data class NewRouteHistory(
    val originLabel: String,
    val destLabel: String,
    val destPlaceId: String?,
    val destLatitude: Double,
    val destLongitude: Double,
    val viaLabel: String?,
    val viaPlaceId: String?,
    val viaLatitude: Double?,
    val viaLongitude: Double?,
    val plannedDurationSeconds: Long,
    val plannedDistanceMeters: Int,
    val encodedPolyline: String,
    val startedAtMillis: Long,
    val hasRouteStats: Boolean = true,
)
