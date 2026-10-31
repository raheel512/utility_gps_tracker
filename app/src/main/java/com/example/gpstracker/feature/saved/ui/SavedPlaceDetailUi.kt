package com.example.gpstracker.feature.saved.ui

import com.example.gpstracker.core.data.saved.SavedPlace

data class SavedPlaceDetailUiState(
    val place: SavedPlace? = null,
    val isLoading: Boolean = true,
)

sealed interface SavedPlaceDetailEvent {
    data object StartTripClicked : SavedPlaceDetailEvent
    data object UnsaveClicked : SavedPlaceDetailEvent
}

sealed interface SavedPlaceDetailEffect {
    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String,
        val destPlaceId: String,
    ) : SavedPlaceDetailEffect
    data object Close : SavedPlaceDetailEffect
}
