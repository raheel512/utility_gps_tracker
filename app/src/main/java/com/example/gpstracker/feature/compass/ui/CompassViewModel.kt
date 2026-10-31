package com.example.gpstracker.feature.compass.ui

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.sensors.HeadingProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CompassViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val headingProvider: HeadingProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CompassEffect>()
    val effects: SharedFlow<CompassEffect> = _effects.asSharedFlow()

    private val location = MutableStateFlow<UserLocation?>(null)
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            locationRepository.getCurrentLocation()?.let(::publishLocation)
            locationRepository.locationUpdates().collect(::publishLocation)
        }
        viewModelScope.launch {
            location
                .map { it?.declinationKey() }
                .distinctUntilChanged()
                .flatMapLatest {
                    headingProvider.headings(location.value?.toAndroidLocation())
                }
                .catch { /* leave the last heading; a missing sensor emits nothing */ }
                .collect { heading ->
                    _uiState.update {
                        it.copy(
                            headingDegrees = heading.degreesTrue,
                            cardinal = heading.cardinal,
                        )
                    }
                }
        }
    }

    fun onEvent(event: CompassEvent) {
        when (event) {
            is CompassEvent.ThemeSelected -> _uiState.update { it.copy(theme = event.theme) }
            CompassEvent.RefreshLocationClicked -> refreshLocation()
        }
    }

    private fun refreshLocation() {
        if (_uiState.value.locating) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(locating = true) }
            try {
                val fix = locationRepository.getCurrentLocation()
                if (fix != null) {
                    publishLocation(fix)
                    _effects.emit(CompassEffect.LocationUpdated)
                }
            } finally {
                _uiState.update { it.copy(locating = false) }
            }
        }
    }

    private fun publishLocation(fix: UserLocation) {
        location.value = fix
        _uiState.update {
            it.copy(
                latitude = fix.latitude,
                longitude = fix.longitude,
                altitudeMeters = fix.altitudeMeters,
            )
        }
    }

    /** ~1 km. Restarts the heading sensor only when declination would change. */
    private fun UserLocation.declinationKey(): Pair<Long, Long> =
        (latitude * 100.0).roundToLong() to (longitude * 100.0).roundToLong()

    private fun UserLocation.toAndroidLocation(): Location =
        Location("compass").also {
            it.latitude = latitude
            it.longitude = longitude
            it.altitude = altitudeMeters ?: 0.0
        }
}

sealed interface CompassEffect {
    data object LocationUpdated : CompassEffect
}
