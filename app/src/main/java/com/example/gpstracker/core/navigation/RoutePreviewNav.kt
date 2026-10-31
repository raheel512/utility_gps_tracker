package com.example.gpstracker.core.navigation

fun routePreviewOf(
    destName: String,
    destLatitude: Double,
    destLongitude: Double,
    destAddress: String? = null,
    destPlaceId: String? = null,
    viaName: String? = null,
    viaPlaceId: String? = null,
    viaLatitude: Double? = null,
    viaLongitude: Double? = null,
): Route.RoutePreview = Route.RoutePreview(
    destName = destName,
    destLatitude = destLatitude,
    destLongitude = destLongitude,
    destAddress = destAddress.orEmpty(),
    destPlaceId = destPlaceId.orEmpty(),
    viaName = viaName.orEmpty(),
    viaPlaceId = viaPlaceId.orEmpty(),
    viaLatitude = viaLatitude,
    viaLongitude = viaLongitude,
)

fun Route.RoutePreview.hasVia(): Boolean =
    viaName.isNotBlank() || (viaLatitude != null && viaLongitude != null)
