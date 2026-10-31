package com.example.gpstracker.core.routes

import com.example.gpstracker.core.places.LatLngPoint

data class RoutePlan(
    val encodedPolyline: String,
    val points: List<LatLngPoint>,
    val distanceMeters: Int,
    val durationSeconds: Long,
    val viaLabel: String?,
)

data class RouteComputeRequest(
    val originLatitude: Double,
    val originLongitude: Double,
    val destName: String,
    val destAddress: String?,
    val destPlaceId: String?,
    val destLatitude: Double,
    val destLongitude: Double,
    val viaName: String? = null,
    val viaPlaceId: String? = null,
    val viaLatitude: Double? = null,
    val viaLongitude: Double? = null,
    val avoidTolls: Boolean = false,
    val avoidFerries: Boolean = false,
)

sealed interface RoutesResult {
    data class Ok(val plan: RoutePlan) : RoutesResult
    data object Empty : RoutesResult
    data object Offline : RoutesResult
    data object Timeout : RoutesResult
    data object Network : RoutesResult
    data class HttpError(val code: Int) : RoutesResult
}

interface RoutesRepository {
    suspend fun computeRoute(request: RouteComputeRequest): RoutesResult
}
