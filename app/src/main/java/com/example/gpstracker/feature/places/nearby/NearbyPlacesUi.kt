package com.example.gpstracker.feature.places.nearby

import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.NearbyCategory
import com.example.gpstracker.core.places.PlaceSummary

data class NearbyPlaceRow(
    val place: PlaceSummary,
    val distanceMeters: Double,
    val distanceDisplay: DistanceFormatter.DistanceDisplay,
)

data class NearbyCategoriesUiState(
    val categories: List<NearbyCategory> = NearbyCategory.seeMoreOrder,
)

data class NearbyResultsUiState(
    val category: NearbyCategory? = null,
    val places: List<NearbyPlaceRow> = emptyList(),
    val isLoading: Boolean = false,
    val emptyResults: Boolean = false,
    val errorMessageRes: Int? = null,
    val showNoInternetDialog: Boolean = false,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val selectedPlaceId: String? = null,
    val isSelectedSaved: Boolean = false,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
)

sealed interface NearbyCategoriesEvent {
    data class CategoryClicked(val category: NearbyCategory) : NearbyCategoriesEvent
}

sealed interface NearbyResultsEvent {
    data class PlaceClicked(val placeId: String) : NearbyResultsEvent
    data object Retry : NearbyResultsEvent
    data object DismissNoInternet : NearbyResultsEvent
    data object ConnectInternet : NearbyResultsEvent
    data object DismissError : NearbyResultsEvent
    data object SaveClicked : NearbyResultsEvent
    data object DirectionsClicked : NearbyResultsEvent
    data object ScreenStopped : NearbyResultsEvent
}

sealed interface NearbyResultsEffect {
    data object OpenWirelessSettings : NearbyResultsEffect
    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String,
        val destPlaceId: String,
    ) : NearbyResultsEffect
}

sealed interface NearbyCategoriesEffect {
    data class OpenCategory(val categoryId: String) : NearbyCategoriesEffect
}
