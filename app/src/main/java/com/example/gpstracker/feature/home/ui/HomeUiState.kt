package com.example.gpstracker.feature.home.ui

import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.places.NearbyCategory

/**
 * Immutable Home screen state. Only connectivity is dynamic today; everything
 * else on Home is static layout driven by string resources and drawables.
 */
data class HomeUiState(
    val connected: Boolean = true,
)

/** Every tappable item on Home. One event type keeps the ViewModel mapping testable. */
enum class HomeTarget {
    // Hero
    RouteFinder,

    // GPS Tools
    VoiceNavigation,
    MyLocation,
    SatelliteView,
    GpsCamera,

    // Nearby Places
    NearbyHotels,
    NearbyCafes,
    NearbyBanks,
    NearbyHospitals,
    NearbySeeMore,

    // Other Feature
    Compass,
    FamousPlaces,
    CountryCode,
    CountryInfo,
    Weather,
    Emergency,
}

sealed interface HomeEvent {
    data class Select(val target: HomeTarget) : HomeEvent
}

/** One-off navigation effect. The ViewModel never touches a NavController. */
sealed interface HomeNavEffect {
    data class Navigate(val route: Route) : HomeNavEffect
    data class OpenNearbyMaps(val category: NearbyCategory) : HomeNavEffect
    data object OpenFamousPlacesMaps : HomeNavEffect
}
