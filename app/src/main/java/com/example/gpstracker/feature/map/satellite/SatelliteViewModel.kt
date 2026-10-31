package com.example.gpstracker.feature.map.satellite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.format.CoordinateFormatter
import com.example.gpstracker.core.map.AppMapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SatelliteUiState(
    val location: UserLocation? = null,
    /** Tooltip above the user dot, e.g. "33.6938° N, 73.0489° E". */
    val coordinates: String? = null,
    val mapType: AppMapType = AppMapType.Satellite,
    val is3d: Boolean = false,
)

sealed interface SatelliteEvent {
    data class SelectMapType(val type: AppMapType) : SatelliteEvent
    data object Toggle3d : SatelliteEvent
    data object ResetOrientation : SatelliteEvent
}

/** Order of the Figma "Map Style Selector Pill"; "Vector" is the SDK's normal map. */
val SatelliteMapStyles = listOf(
    AppMapType.Satellite,
    AppMapType.Hybrid,
    AppMapType.Terrain,
    AppMapType.Normal,
)

@HiltViewModel
class SatelliteViewModel @Inject constructor(
    locationRepository: LocationRepository,
) : ViewModel() {

    private data class Controls(val mapType: AppMapType = AppMapType.Satellite, val is3d: Boolean = false)

    private val controls = MutableStateFlow(Controls())

    private val locations = flow<UserLocation?> {
        emit(null)
        locationRepository.getCurrentLocation()?.let { emit(it) }
        emitAll(locationRepository.locationUpdates())
    }

    val uiState: StateFlow<SatelliteUiState> = combine(locations, controls) { location, controls ->
        SatelliteUiState(
            location = location,
            coordinates = location?.let { CoordinateFormatter.format(it.latitude, it.longitude) },
            mapType = controls.mapType,
            is3d = controls.is3d,
        )
    }.onStart { emit(SatelliteUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SatelliteUiState())

    fun onEvent(event: SatelliteEvent) {
        when (event) {
            is SatelliteEvent.SelectMapType -> controls.update { it.copy(mapType = event.type) }
            SatelliteEvent.Toggle3d -> controls.update { it.copy(is3d = !it.is3d) }
            SatelliteEvent.ResetOrientation -> controls.update { it.copy(is3d = false) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
