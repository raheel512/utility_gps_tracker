package com.example.gpstracker.core.location.gate

import com.example.gpstracker.core.permissions.LocationPermissionState

/**
 * Resolved gate status for a screen that needs location.
 *
 * Order: network (optional) → permission → permanent denial → GPS off → ready.
 */
sealed interface LocationGatePhase {
    data object Checking : LocationGatePhase
    data object NeedsNetwork : LocationGatePhase
    data object NeedsPermission : LocationGatePhase
    data object PermanentlyDenied : LocationGatePhase
    data object GpsOff : LocationGatePhase
    data object Ready : LocationGatePhase
}

/**
 * Pure ordering used by [LocationGateViewModel] and unit tests.
 *
 * Null inputs mean that slice of state has not been observed yet → [Checking].
 */
fun resolveLocationGatePhase(
    requiresNetwork: Boolean,
    isConnected: Boolean?,
    permission: LocationPermissionState?,
    isLocationEnabled: Boolean?,
): LocationGatePhase {
    if (requiresNetwork) {
        when (isConnected) {
            null -> return LocationGatePhase.Checking
            false -> return LocationGatePhase.NeedsNetwork
            true -> Unit
        }
    }

    when (permission) {
        null -> return LocationGatePhase.Checking
        LocationPermissionState.NotDetermined,
        LocationPermissionState.Denied,
        -> return LocationGatePhase.NeedsPermission
        LocationPermissionState.PermanentlyDenied -> return LocationGatePhase.PermanentlyDenied
        is LocationPermissionState.Granted -> Unit
    }

    return when (isLocationEnabled) {
        null -> LocationGatePhase.Checking
        false -> LocationGatePhase.GpsOff
        true -> LocationGatePhase.Ready
    }
}
