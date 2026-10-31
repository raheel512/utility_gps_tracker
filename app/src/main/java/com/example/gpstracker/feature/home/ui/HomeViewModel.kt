package com.example.gpstracker.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.places.NearbyCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = connectivityRepository.isConnected
        .map { HomeUiState(connected = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeUiState(),
        )

    private val navEffects = EventBus<HomeNavEffect>()
    val effects = navEffects.events

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Select -> {
                val category = event.target.nearbyCategoryOrNull()
                if (category != null) {
                    viewModelScope.launch { navEffects.emit(HomeNavEffect.OpenNearbyMaps(category)) }
                    return
                }
                if (event.target == HomeTarget.FamousPlaces) {
                    viewModelScope.launch { navEffects.emit(HomeNavEffect.OpenFamousPlacesMaps) }
                    return
                }
                val route = event.target.toRouteOrNull()
                if (route != null) {
                    viewModelScope.launch { navEffects.emit(HomeNavEffect.Navigate(route)) }
                }
                // else: no destination exists yet — see TODO(route) in toRouteOrNull().
            }
        }
    }
}

/**
 * Maps a [HomeTarget] to an existing [Route], or `null` when no route exists yet.
 * Home nearby tiles and Famous Places open Google Maps. See More opens the category grid.
 */
internal fun HomeTarget.nearbyCategoryOrNull(): NearbyCategory? = when (this) {
    HomeTarget.NearbyHotels -> NearbyCategory.Hotels
    HomeTarget.NearbyCafes -> NearbyCategory.CcdCafe
    HomeTarget.NearbyBanks -> NearbyCategory.Bank
    HomeTarget.NearbyHospitals -> NearbyCategory.Hospital
    else -> null
}

internal fun HomeTarget.toRouteOrNull(): Route? = when (this) {
    HomeTarget.RouteFinder -> Route.RouteFinder
    HomeTarget.VoiceNavigation -> Route.Voice
    HomeTarget.MyLocation -> Route.MyLocation
    HomeTarget.SatelliteView -> Route.SatelliteMap
    HomeTarget.GpsCamera -> Route.GpsCamera
    HomeTarget.NearbyHotels,
    HomeTarget.NearbyCafes,
    HomeTarget.NearbyBanks,
    HomeTarget.NearbyHospitals,
    -> null
    HomeTarget.NearbySeeMore -> Route.NearbyPlaces
    HomeTarget.Compass -> Route.Compass
    HomeTarget.FamousPlaces -> null
    HomeTarget.CountryCode -> Route.CountryCode
    HomeTarget.CountryInfo -> Route.CountryInfo
    HomeTarget.Weather -> Route.Weather
    HomeTarget.Emergency -> Route.Emergency
}
