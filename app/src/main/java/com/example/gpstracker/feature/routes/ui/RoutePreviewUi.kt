package com.example.gpstracker.feature.routes.ui

import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.core.routes.RoutePlan

data class RoutePreviewUiState(
    val destName: String = "",
    val destAddress: String = "",
    val destPlaceId: String = "",
    val destLatitude: Double = 0.0,
    val destLongitude: Double = 0.0,
    val viaLabel: String? = null,
    val originLabel: String? = null,
    val originLatitude: Double? = null,
    val originLongitude: Double? = null,
    val plan: RoutePlan? = null,
    val duration: DurationFormatter.Display? = null,
    val distance: DistanceFormatter.DistanceDisplay? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val canStart: Boolean = false,
    val showNoInternetDialog: Boolean = false,
    val errorMessageRes: Int? = null,
    val snackbarMessageRes: Int? = null,
    val addStopVisible: Boolean = false,
    val addStopQuery: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val avoidTolls: Boolean = false,
    val avoidFerries: Boolean = false,
)

sealed interface RoutePreviewEvent {
    data object Refresh : RoutePreviewEvent
    data object StartNavigationClicked : RoutePreviewEvent
    data object SaveClicked : RoutePreviewEvent
    data object AddStopClicked : RoutePreviewEvent
    data object DismissAddStop : RoutePreviewEvent
    data class AddStopQueryChanged(val query: String) : RoutePreviewEvent
    data class ViaSuggestionSelected(val suggestion: PlaceSuggestion) : RoutePreviewEvent
    data object StartLaunched : RoutePreviewEvent
    data object StartFailed : RoutePreviewEvent
    data object DismissNoInternet : RoutePreviewEvent
    data object ConnectInternet : RoutePreviewEvent
    data object DismissError : RoutePreviewEvent
    data object DismissSnackbar : RoutePreviewEvent
    data object ScreenStopped : RoutePreviewEvent
}

sealed interface RoutePreviewEffect {
    data class OpenMaps(val url: String) : RoutePreviewEffect
    data object OpenWirelessSettings : RoutePreviewEffect
}
