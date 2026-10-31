package com.example.gpstracker.feature.voice.ui

import com.example.gpstracker.core.permissions.MicrophonePermissionState
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.core.routes.RoutePlan

enum class VoicePhase {
    Idle,
    Listening,
    Resolving,
    Results,
}

data class VoicePlace(
    val name: String,
    val address: String,
    val placeId: String,
    val latitude: Double,
    val longitude: Double,
)

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.Idle,
    val level: Float = 0f,
    val partialText: String = "",
    val transcript: String = "",
    val destination: VoicePlace? = null,
    val via: VoicePlace? = null,
    val plan: RoutePlan? = null,
    val duration: DurationFormatter.Display? = null,
    val distance: DistanceFormatter.DistanceDisplay? = null,
    val isSaved: Boolean = false,
    val showMicRationale: Boolean = false,
    val micPermanentlyDenied: Boolean = false,
    val showNoInternetDialog: Boolean = false,
    val snackbarMessageRes: Int? = null,
    val canStart: Boolean = false,
)

sealed interface VoiceEvent {
    data class ScreenOpened(
        val permission: MicrophonePermissionState,
        val returnTextOnly: Boolean = false,
    ) : VoiceEvent
    data object MicClicked : VoiceEvent
    data class PermissionResult(val permission: MicrophonePermissionState) : VoiceEvent
    data object DismissMicRationale : VoiceEvent
    data object AllowMicClicked : VoiceEvent
    data object CloseClicked : VoiceEvent
    data object ClearTranscript : VoiceEvent
    data object StartNavigationClicked : VoiceEvent
    data object StartLaunched : VoiceEvent
    data object StartFailed : VoiceEvent
    data object PreviewClicked : VoiceEvent
    data object AddStopClicked : VoiceEvent
    data class ViaPicked(val place: VoicePlace) : VoiceEvent
    data object SaveClicked : VoiceEvent
    data object DismissNoInternet : VoiceEvent
    data object ConnectInternet : VoiceEvent
    data object DismissSnackbar : VoiceEvent
    data object AppBackgrounded : VoiceEvent
    data object ScreenLeft : VoiceEvent
    data object SessionDismissed : VoiceEvent
}

sealed interface VoiceEffect {
    data object NavigateUp : VoiceEffect
    data object OpenAppSettings : VoiceEffect
    data object OpenWirelessSettings : VoiceEffect
    data object RequestMicPermission : VoiceEffect
    data object OpenPickStop : VoiceEffect
    data class OpenMaps(val url: String) : VoiceEffect
    data class FillDestination(
        val label: String,
        val placeId: String = "",
        val address: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
    ) : VoiceEffect

    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String,
        val destPlaceId: String,
        val viaName: String,
        val viaPlaceId: String,
        val viaLatitude: Double?,
        val viaLongitude: Double?,
    ) : VoiceEffect
}
