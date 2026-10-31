package com.example.gpstracker.core.location.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.location.DeviceLocationSettings
import com.example.gpstracker.core.location.LocationSettingsResolution
import com.example.gpstracker.core.permissions.LocationPermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationGateUiState(
    val phase: LocationGatePhase = LocationGatePhase.Checking,
    val permission: LocationPermissionState? = null,
    val hasAskedPermission: Boolean = false,
    /** Soft mode: user dismissed prompts; content stays; location fields may be omitted. */
    val softSkipped: Boolean = false,
)

sealed interface LocationGateEvent {
    data class Configure(
        val requiresNetwork: Boolean,
        val softMode: Boolean = false,
    ) : LocationGateEvent

    data class PermissionStateChanged(val state: LocationPermissionState) : LocationGateEvent
    data object AllowPermissionClicked : LocationGateEvent
    data object OpenSettingsClicked : LocationGateEvent
    data object EnableGpsClicked : LocationGateEvent
    data object DismissClicked : LocationGateEvent
    data object ConnectInternetClicked : LocationGateEvent
    data object Refresh : LocationGateEvent
}

sealed interface LocationGateEffect {
    data object RequestPermission : LocationGateEffect
    data object OpenAppSettings : LocationGateEffect
    data class ResolveGpsSettings(
        val resolution: LocationSettingsResolution.Resolvable,
    ) : LocationGateEffect
    data object OpenWirelessSettings : LocationGateEffect
    data object Dismiss : LocationGateEffect
}

@HiltViewModel
class LocationGateViewModel @Inject constructor(
    private val connectivityRepository: ConnectivityRepository,
    private val deviceLocationSettings: DeviceLocationSettings,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val requiresNetwork = MutableStateFlow(false)
    private val softMode = MutableStateFlow(false)
    private val softSkipped = MutableStateFlow(false)
    private val permissionState = MutableStateFlow<LocationPermissionState?>(null)

    private val effectBus = EventBus<LocationGateEffect>()
    val effects = effectBus.events

    val uiState: StateFlow<LocationGateUiState> = combine(
        combine(
            requiresNetwork,
            connectivityRepository.isConnected,
            permissionState,
            deviceLocationSettings.isLocationEnabled,
            settingsRepository.locationPermissionAsked,
        ) { needsNet, connected, permission, gpsOn, hasAsked ->
            LocationGateUiState(
                phase = resolveLocationGatePhase(
                    requiresNetwork = needsNet,
                    isConnected = connected,
                    permission = permission,
                    isLocationEnabled = gpsOn,
                ),
                permission = permission,
                hasAskedPermission = hasAsked,
            )
        },
        softSkipped,
    ) { base, skipped ->
        base.copy(softSkipped = skipped)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LocationGateUiState(),
    )

    fun onEvent(event: LocationGateEvent) {
        when (event) {
            is LocationGateEvent.Configure -> {
                requiresNetwork.value = event.requiresNetwork
                softMode.value = event.softMode
                if (!event.softMode) softSkipped.value = false
            }
            is LocationGateEvent.PermissionStateChanged -> {
                permissionState.value = event.state
            }
            LocationGateEvent.AllowPermissionClicked -> {
                viewModelScope.launch { effectBus.emit(LocationGateEffect.RequestPermission) }
            }
            LocationGateEvent.OpenSettingsClicked -> {
                viewModelScope.launch { effectBus.emit(LocationGateEffect.OpenAppSettings) }
            }
            LocationGateEvent.EnableGpsClicked -> {
                viewModelScope.launch {
                    when (val resolution = deviceLocationSettings.checkLocationSettings()) {
                        is LocationSettingsResolution.Resolvable -> {
                            effectBus.emit(LocationGateEffect.ResolveGpsSettings(resolution))
                        }
                        LocationSettingsResolution.Satisfied -> Unit
                        LocationSettingsResolution.Unresolvable -> Unit
                    }
                }
            }
            LocationGateEvent.DismissClicked -> {
                if (softMode.value) {
                    // Design review: Not Now must not leave the camera; stamp omits location.
                    softSkipped.value = true
                } else {
                    viewModelScope.launch { effectBus.emit(LocationGateEffect.Dismiss) }
                }
            }
            LocationGateEvent.ConnectInternetClicked -> {
                viewModelScope.launch { effectBus.emit(LocationGateEffect.OpenWirelessSettings) }
            }
            LocationGateEvent.Refresh -> Unit
        }
    }

    fun markPermissionAsked() {
        viewModelScope.launch {
            settingsRepository.setLocationPermissionAsked(true)
        }
    }
}
