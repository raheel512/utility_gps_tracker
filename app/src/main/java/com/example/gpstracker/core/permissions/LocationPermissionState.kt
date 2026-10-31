package com.example.gpstracker.core.permissions

/**
 * Runtime location permission snapshot for ACCESS_FINE / ACCESS_COARSE.
 */
sealed interface LocationPermissionState {
    /** At least coarse granted. [isPrecise] is true when fine is also granted. */
    data class Granted(val isPrecise: Boolean) : LocationPermissionState

    /** Denied but the system will show the rationale / ask again. */
    data object Denied : LocationPermissionState

    /** Denied with "don't ask again" (or equivalent). Open app settings. */
    data object PermanentlyDenied : LocationPermissionState

    /** Never requested by this app install (DataStore has-asked flag is false). */
    data object NotDetermined : LocationPermissionState
}

/**
 * Pure resolver for unit tests and Compose helpers.
 *
 * Permanent denial: has asked before AND neither permission is granted AND
 * [shouldShowRationale] is false.
 */
fun resolveLocationPermissionState(
    fineGranted: Boolean,
    coarseGranted: Boolean,
    shouldShowRationale: Boolean,
    hasAsked: Boolean,
): LocationPermissionState = when {
    fineGranted -> LocationPermissionState.Granted(isPrecise = true)
    coarseGranted -> LocationPermissionState.Granted(isPrecise = false)
    !hasAsked -> LocationPermissionState.NotDetermined
    shouldShowRationale -> LocationPermissionState.Denied
    else -> LocationPermissionState.PermanentlyDenied
}
