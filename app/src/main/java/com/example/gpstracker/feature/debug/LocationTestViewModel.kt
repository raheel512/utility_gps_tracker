package com.example.gpstracker.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.gate.LocationGatePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationTestUiState(
    val gatePhase: LocationGatePhase = LocationGatePhase.Checking,
    val latestLocation: UserLocation? = null,
    val isCollecting: Boolean = false,
)

sealed interface LocationTestEvent {
    data class GatePhaseChanged(val phase: LocationGatePhase) : LocationTestEvent
}

@HiltViewModel
class LocationTestViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationTestUiState())
    val uiState: StateFlow<LocationTestUiState> = _uiState.asStateFlow()

    private var updatesJob: Job? = null

    fun onEvent(event: LocationTestEvent) {
        when (event) {
            is LocationTestEvent.GatePhaseChanged -> {
                _uiState.update { it.copy(gatePhase = event.phase) }
                if (event.phase == LocationGatePhase.Ready) {
                    startUpdates()
                } else {
                    stopUpdates()
                }
            }
        }
    }

    private fun startUpdates() {
        if (updatesJob?.isActive == true) return
        _uiState.update { it.copy(isCollecting = true) }
        updatesJob = viewModelScope.launch {
            val current = locationRepository.getCurrentLocation()
            if (current != null) {
                _uiState.update { it.copy(latestLocation = current) }
            }
            locationRepository.locationUpdates().collect { fix ->
                _uiState.update { it.copy(latestLocation = fix) }
            }
        }
    }

    private fun stopUpdates() {
        updatesJob?.cancel()
        updatesJob = null
        _uiState.update { it.copy(isCollecting = false) }
    }
}
