package com.example.gpstracker.core.places

/**
 * Domain models for Places. SDK types must not leak into ViewModels.
 */
data class PlaceSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
)

data class PlaceSummary(
    val placeId: String,
    val name: String,
    val formattedAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val primaryType: String? = null,
)

data class PlaceDetails(
    val placeId: String,
    val name: String,
    val formattedAddress: String?,
    val latitude: Double,
    val longitude: Double,
)

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double,
)

/** Opaque autocomplete session; create via [PlacesRepository.newAutocompleteSession]. */
interface AutocompleteSession

sealed interface PlacesResult<out T> {
    data class Ok<T>(val value: T) : PlacesResult<T>
    data class Failure(
        val reason: PlacesFailureReason,
        val cause: Throwable? = null,
    ) : PlacesResult<Nothing>
}

enum class PlacesFailureReason {
    NotInitialized,
    ApiKeyOrApiDisabled,
    Network,
    Empty,
    Unknown,
}
