package com.example.gpstracker.core.location.geocode

/**
 * Address parts returned by the platform geocoder. Only fields the geocoder actually
 * returned are kept; nothing is filled in locally.
 */
data class GeocodedAddress(
    val thoroughfare: String? = null,
    val subLocality: String? = null,
    val locality: String? = null,
    val countryName: String? = null,
) {
    /** "9th Ave, F-9, Islamabad, Pakistan" — null when the geocoder gave nothing usable. */
    fun displayLine(): String? = listOf(thoroughfare, subLocality, locality, countryName)
        .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
        .distinct()
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = ", ")

    /** Neighborhood, then city, then country. Null when nothing was returned. */
    fun placeLabel(): String? = listOf(subLocality, locality, countryName, thoroughfare)
        .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
        .distinct()
        .firstOrNull()
}

interface ReverseGeocoder {
    /** Returns null when offline, no geocoder backend exists, or no address was found. */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedAddress?
}
