package com.example.gpstracker.feature.search.ui

import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion

data class FindLocationUiState(
    val myLocationLine: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val locationRevision: Int = 0,
    val locating: Boolean = false,
    val destinationQuery: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlace: PlaceDetails? = null,
    val isSelectedSaved: Boolean = false,
    val showNoInternetDialog: Boolean = false,
    val errorMessageRes: Int? = null,
    val emptyResults: Boolean = false,
)

sealed interface FindLocationEvent {
    data class DestinationQueryChanged(val query: String) : FindLocationEvent
    data class VoicePlaceChosen(
        val name: String,
        val placeId: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
    ) : FindLocationEvent
    data class SuggestionSelected(val suggestion: PlaceSuggestion) : FindLocationEvent
    data object SwapClicked : FindLocationEvent
    data object UseGpsClicked : FindLocationEvent
    data object VoiceSearchClicked : FindLocationEvent
    data object NavigateClicked : FindLocationEvent
    data object SaveClicked : FindLocationEvent
    data object ClearSelection : FindLocationEvent
    data object DismissNoInternet : FindLocationEvent
    data object ConnectInternet : FindLocationEvent
    data object DismissError : FindLocationEvent
    data object ScreenStopped : FindLocationEvent
}

sealed interface FindLocationEffect {
    data object OpenWirelessSettings : FindLocationEffect
    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String,
        val destPlaceId: String,
    ) : FindLocationEffect
    data object OpenVoice : FindLocationEffect
}
