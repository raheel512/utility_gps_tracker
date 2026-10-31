package com.example.gpstracker.core.data.saved

/**
 * Domain models for saved places. Room entities must not reach the UI layer.
 */
data class SavedPlace(
    val id: Long,
    val placeId: String?,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val collectionId: Long?,
    val createdAtMillis: Long,
    val iconKind: SavedPlaceIconKind,
)

enum class SavedPlaceIconKind {
    HOME,
    WORK,
    PLACE,
}

data class PlaceCollection(
    val id: Long,
    val name: String,
    val createdAtMillis: Long,
    val placeCount: Int = 0,
)

/**
 * Input for [SavedPlacesRepository.toggleSaved] / save.
 */
data class SavePlaceRequest(
    val placeId: String?,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val collectionId: Long? = null,
    val iconKind: SavedPlaceIconKind = SavedPlaceIconKind.PLACE,
)

sealed interface ToggleSavedResult {
    data class Saved(val place: SavedPlace) : ToggleSavedResult
    data class Removed(val place: SavedPlace) : ToggleSavedResult
}
