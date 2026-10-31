package com.example.gpstracker.feature.voice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.data.history.NewRouteHistory
import com.example.gpstracker.core.data.history.RouteHistoryRepository
import com.example.gpstracker.core.data.saved.SavePlaceRequest
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.data.saved.ToggleSavedResult
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.format.CoordinateFormatter
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.core.permissions.MicrophonePermissionState
import com.example.gpstracker.core.places.AutocompleteSessionManager
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlacesFailureReason
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.core.routes.GeoUri
import com.example.gpstracker.core.routes.RouteComputeRequest
import com.example.gpstracker.core.routes.RoutePlan
import com.example.gpstracker.core.routes.RoutesRepository
import com.example.gpstracker.core.routes.RoutesResult
import com.example.gpstracker.core.speech.SpeechError
import com.example.gpstracker.core.speech.SpeechRecognizerClient
import com.example.gpstracker.core.speech.SpeechSession
import com.example.gpstracker.core.speech.SpeechState
import com.example.gpstracker.feature.voice.VoiceCommand
import com.example.gpstracker.feature.voice.VoiceCommandParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val speechClient: SpeechRecognizerClient,
    private val placesRepository: PlacesRepository,
    private val sessionManager: AutocompleteSessionManager,
    private val routesRepository: RoutesRepository,
    private val historyRepository: RouteHistoryRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val reverseGeocoder: ReverseGeocoder,
    private val clock: Clock,
) : ViewModel() {

    private val speechSession = SpeechSession(speechClient)
    private var speechJob: Job? = null
    private var opened = false
    private var holdSession = false
    private var returnTextOnly = false
    private var micAsked = false
    private var micPermission: MicrophonePermissionState = MicrophonePermissionState.NotDetermined
    private var routeAttempted = false
    private var originLabel: String = ""
    private var originLatitude: Double? = null
    private var originLongitude: Double? = null
    private var avoidTolls = false
    private var avoidFerries = false

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<VoiceEffect>()
    val effects: Flow<VoiceEffect> = effectBus.events

    fun onEvent(event: VoiceEvent) {
        when (event) {
            is VoiceEvent.ScreenOpened -> {
                returnTextOnly = event.returnTextOnly
                if (opened) {
                    holdSession = false
                    return
                }
                opened = true
                holdSession = false
                onPermission(event.permission, fromOpen = true)
            }
            VoiceEvent.MicClicked -> onMicClicked()
            is VoiceEvent.PermissionResult -> {
                micPermission = event.permission
                onPermission(event.permission, fromOpen = false)
            }
            VoiceEvent.DismissMicRationale -> {
                _uiState.update { it.copy(showMicRationale = false) }
            }
            VoiceEvent.AllowMicClicked -> {
                if (_uiState.value.micPermanentlyDenied) {
                    effectBus.tryEmit(VoiceEffect.OpenAppSettings)
                } else {
                    micAsked = true
                    effectBus.tryEmit(VoiceEffect.RequestMicPermission)
                }
            }
            VoiceEvent.CloseClicked -> {
                opened = false
                viewModelScope.coroutineContext.cancelChildren()
                stopListening(returnToIdle = true)
                _uiState.value = VoiceUiState()
                effectBus.tryEmit(VoiceEffect.NavigateUp)
            }
            VoiceEvent.ClearTranscript -> {
                routeAttempted = false
                _uiState.update {
                    it.copy(
                        phase = VoicePhase.Idle,
                        transcript = "",
                        destination = null,
                        via = null,
                        plan = null,
                        duration = null,
                        distance = null,
                        canStart = false,
                    )
                }
                startListening()
            }
            VoiceEvent.StartNavigationClicked -> startNavigation()
            VoiceEvent.StartLaunched -> recordHistory()
            VoiceEvent.StartFailed -> {
                _uiState.update { it.copy(snackbarMessageRes = R.string.routes_maps_not_installed) }
            }
            VoiceEvent.PreviewClicked -> openPreview()
            VoiceEvent.AddStopClicked -> {
                holdSession = true
                effectBus.tryEmit(VoiceEffect.OpenPickStop)
            }
            is VoiceEvent.ViaPicked -> applyVia(event.place)
            VoiceEvent.SaveClicked -> toggleSave()
            VoiceEvent.DismissNoInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
            }
            VoiceEvent.ConnectInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
                effectBus.tryEmit(VoiceEffect.OpenWirelessSettings)
            }
            VoiceEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessageRes = null) }
            VoiceEvent.AppBackgrounded -> releaseRecognizer()
            VoiceEvent.ScreenLeft -> releaseRecognizer()
            VoiceEvent.SessionDismissed -> {
                releaseRecognizer()
                if (!holdSession) {
                    viewModelScope.coroutineContext.cancelChildren()
                    opened = false
                    _uiState.value = VoiceUiState()
                }
            }
        }
    }

    override fun onCleared() {
        speechSession.destroy()
        super.onCleared()
    }

    private fun onMicClicked() {
        if (_uiState.value.phase == VoicePhase.Listening) {
            stopListening(returnToIdle = true)
            return
        }
        when (micPermission) {
            MicrophonePermissionState.Granted -> startListening()
            MicrophonePermissionState.PermanentlyDenied -> {
                _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = true) }
            }
            MicrophonePermissionState.Denied,
            MicrophonePermissionState.NotDetermined,
            -> _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = false) }
        }
    }

    private fun onPermission(permission: MicrophonePermissionState, fromOpen: Boolean) {
        micPermission = permission
        when (permission) {
            MicrophonePermissionState.Granted -> {
                _uiState.update { it.copy(showMicRationale = false, micPermanentlyDenied = false) }
                if (fromOpen || _uiState.value.phase == VoicePhase.Idle) startListening()
            }
            MicrophonePermissionState.NotDetermined -> {
                _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = false) }
            }
            MicrophonePermissionState.Denied -> {
                _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = false) }
            }
            MicrophonePermissionState.PermanentlyDenied -> {
                _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = true) }
            }
        }
    }

    private fun startListening() {
        if (!speechClient.isRecognitionAvailable()) {
            _uiState.update { it.copy(snackbarMessageRes = R.string.voice_unavailable, phase = VoicePhase.Idle) }
            return
        }
        viewModelScope.launch {
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update { it.copy(showNoInternetDialog = true, phase = VoicePhase.Idle) }
                return@launch
            }
            val language = settingsRepository.selectedLanguageCode.first()
                .ifBlank { Locale.getDefault().toLanguageTag() }
            _uiState.update {
                it.copy(
                    phase = VoicePhase.Listening,
                    level = 0f,
                    partialText = "",
                    snackbarMessageRes = null,
                )
            }
            observeSpeech()
            speechSession.start(viewModelScope, language)
        }
    }

    private fun observeSpeech() {
        speechJob?.cancel()
        speechJob = viewModelScope.launch {
            speechSession.states.collect { state ->
                when (state) {
                    SpeechState.Idle -> Unit
                    is SpeechState.Listening -> _uiState.update {
                        it.copy(phase = VoicePhase.Listening, partialText = state.partialText, level = state.level)
                    }
                    SpeechState.Processing -> _uiState.update { it.copy(phase = VoicePhase.Resolving) }
                    is SpeechState.Result -> resolve(state.transcripts.take(MAX_ALTERNATIVES))
                    is SpeechState.Error -> onSpeechError(state.reason)
                }
            }
        }
    }

    private fun onSpeechError(reason: SpeechError) {
        stopListening(returnToIdle = true)
        when (reason) {
            SpeechError.Network -> _uiState.update { it.copy(showNoInternetDialog = true) }
            SpeechError.InsufficientPermissions -> {
                _uiState.update { it.copy(showMicRationale = true, micPermanentlyDenied = true) }
            }
            SpeechError.NoMatch, SpeechError.SpeechTimeout -> {
                _uiState.update { it.copy(snackbarMessageRes = R.string.voice_nothing_heard) }
            }
            SpeechError.Busy,
            SpeechError.Client,
            SpeechError.Server,
            SpeechError.Audio,
            -> _uiState.update { it.copy(snackbarMessageRes = R.string.voice_unavailable) }
        }
    }

    private fun releaseRecognizer() {
        if (_uiState.value.phase == VoicePhase.Listening ||
            _uiState.value.phase == VoicePhase.Resolving
        ) {
            stopListening(returnToIdle = true)
        } else {
            speechSession.destroy()
        }
    }

    private fun stopListening(returnToIdle: Boolean) {
        speechJob?.cancel()
        speechSession.destroy()
        if (returnToIdle && _uiState.value.phase != VoicePhase.Results) {
            _uiState.update { it.copy(phase = VoicePhase.Idle, level = 0f) }
        }
    }

    private fun resolve(transcripts: List<String>) {
        speechSession.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(phase = VoicePhase.Resolving) }
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update { it.copy(phase = VoicePhase.Idle, showNoInternetDialog = true) }
                return@launch
            }
            val bias = locationRepository.getCurrentLocation()?.let {
                LatLngPoint(it.latitude, it.longitude)
            }
            for (transcript in transcripts) {
                val command = VoiceCommandParser.parse(transcript)
                if (command.destinationQuery.length < PlacesRepository.AUTOCOMPLETE_MIN_CHARS) continue
                val destination = resolveQuery(command.destinationQuery, bias) ?: continue
                if (returnTextOnly) {
                    deliverToField(destination.name, destination)
                    return@launch
                }
                val via = command.viaQuery
                    ?.takeIf { it.length >= PlacesRepository.AUTOCOMPLETE_MIN_CHARS }
                    ?.let { resolveQuery(it, bias) }
                showResults(command, destination, via)
                return@launch
            }
            if (returnTextOnly) {
                val spoken = transcripts.firstOrNull()?.trim().orEmpty()
                if (spoken.isNotEmpty()) {
                    deliverToField(spoken, place = null)
                } else {
                    _uiState.update {
                        it.copy(phase = VoicePhase.Idle, snackbarMessageRes = R.string.voice_no_matches)
                    }
                }
                return@launch
            }
            _uiState.update {
                it.copy(phase = VoicePhase.Idle, snackbarMessageRes = R.string.voice_no_matches)
            }
        }
    }

    private suspend fun resolveQuery(query: String, bias: LatLngPoint?): VoicePlace? {
        val suggestions = when (
            val result = placesRepository.findAutocompletePredictions(
                query = query,
                session = sessionManager.current(),
                locationBias = bias,
            )
        ) {
            is PlacesResult.Ok -> result.value
            is PlacesResult.Failure -> {
                if (result.reason == PlacesFailureReason.Network) {
                    _uiState.update { it.copy(showNoInternetDialog = true) }
                }
                emptyList()
            }
        }
        val top = suggestions.firstOrNull() ?: return null
        val details = when (
            val result = placesRepository.fetchPlaceDetails(
                placeId = top.placeId,
                displayName = top.primaryText,
                session = sessionManager.current(),
            )
        ) {
            is PlacesResult.Ok -> result.value
            is PlacesResult.Failure -> return null
        }
        sessionManager.reset()
        return VoicePlace(
            name = details.name,
            address = details.formattedAddress.orEmpty(),
            placeId = details.placeId,
            latitude = details.latitude,
            longitude = details.longitude,
        )
    }

    private fun deliverToField(label: String, place: VoicePlace?) {
        effectBus.tryEmit(
            VoiceEffect.FillDestination(
                label = label,
                placeId = place?.placeId.orEmpty(),
                address = place?.address.orEmpty(),
                latitude = place?.latitude,
                longitude = place?.longitude,
            ),
        )
        _uiState.update { it.copy(phase = VoicePhase.Idle, level = 0f) }
    }

    private fun showResults(command: VoiceCommand, destination: VoicePlace, via: VoicePlace?) {
        routeAttempted = false
        _uiState.update {
            it.copy(
                phase = VoicePhase.Results,
                transcript = command.originalText.ifBlank { command.destinationQuery },
                destination = destination,
                via = via,
                canStart = true,
                level = 0f,
            )
        }
        observeSaved(destination)
        computeRoute()
    }

    private fun applyVia(place: VoicePlace) {
        _uiState.update { it.copy(via = place) }
        routeAttempted = false
        computeRoute()
    }

    private fun computeRoute() {
        val destination = _uiState.value.destination ?: return
        viewModelScope.launch {
            routeAttempted = true
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update { it.copy(showNoInternetDialog = true, plan = null, duration = null, distance = null) }
                return@launch
            }
            val fix = locationRepository.getCurrentLocation()
            if (fix != null) {
                originLatitude = fix.latitude
                originLongitude = fix.longitude
                originLabel = reverseGeocoder.reverseGeocode(fix.latitude, fix.longitude)?.locality
                    ?: CoordinateFormatter.format(fix.latitude, fix.longitude)
            }
            avoidTolls = settingsRepository.avoidTolls.first()
            avoidFerries = settingsRepository.avoidFerries.first()
            val unit = settingsRepository.distanceUnit.first()
            val via = _uiState.value.via
            if (fix == null) {
                _uiState.update { it.copy(plan = null, duration = null, distance = null) }
                return@launch
            }
            val request = routeRequest(fix.latitude, fix.longitude, destination, via)
            when (val result = routesRepository.computeRoute(request)) {
                is RoutesResult.Ok -> applyPlan(result.plan, unit)
                else -> _uiState.update { it.copy(plan = null, duration = null, distance = null) }
            }
        }
    }

    private fun routeRequest(
        originLat: Double,
        originLng: Double,
        destination: VoicePlace,
        via: VoicePlace?,
    ) = RouteComputeRequest(
        originLatitude = originLat,
        originLongitude = originLng,
        destName = destination.name,
        destAddress = destination.address.ifBlank { null },
        destPlaceId = destination.placeId.ifBlank { null },
        destLatitude = destination.latitude,
        destLongitude = destination.longitude,
        viaName = via?.name,
        viaPlaceId = via?.placeId?.ifBlank { null },
        viaLatitude = via?.latitude,
        viaLongitude = via?.longitude,
        avoidTolls = avoidTolls,
        avoidFerries = avoidFerries,
    )

    private fun applyPlan(plan: RoutePlan, unit: com.example.gpstracker.core.data.DistanceUnit) {
        _uiState.update {
            it.copy(
                plan = plan,
                duration = DurationFormatter.display(plan.durationSeconds),
                distance = DistanceFormatter.format(plan.distanceMeters.toDouble(), unit),
            )
        }
    }

    private fun startNavigation() {
        val state = _uiState.value
        val destination = state.destination ?: return
        if (!state.canStart) return
        val url = GeoUri.pin(
            latitude = destination.latitude,
            longitude = destination.longitude,
            label = destination.name,
        )
        effectBus.tryEmit(VoiceEffect.OpenMaps(url))
    }

    private fun recordHistory() {
        val state = _uiState.value
        val destination = state.destination ?: return
        val plan = state.plan
        viewModelScope.launch {
            historyRepository.record(
                NewRouteHistory(
                    originLabel = originLabel.ifBlank { destination.name },
                    destLabel = destination.name,
                    destPlaceId = destination.placeId.ifBlank { null },
                    destLatitude = destination.latitude,
                    destLongitude = destination.longitude,
                    viaLabel = state.via?.name,
                    viaPlaceId = state.via?.placeId?.ifBlank { null },
                    viaLatitude = state.via?.latitude,
                    viaLongitude = state.via?.longitude,
                    plannedDurationSeconds = plan?.durationSeconds ?: 0,
                    plannedDistanceMeters = plan?.distanceMeters ?: 0,
                    encodedPolyline = plan?.encodedPolyline.orEmpty(),
                    startedAtMillis = clock.millis(),
                    hasRouteStats = plan != null,
                ),
            )
        }
    }

    private fun openPreview() {
        val state = _uiState.value
        val destination = state.destination ?: return
        val via = state.via
        effectBus.tryEmit(
            VoiceEffect.OpenRoutePreview(
                destName = destination.name,
                destLatitude = destination.latitude,
                destLongitude = destination.longitude,
                destAddress = destination.address,
                destPlaceId = destination.placeId,
                viaName = via?.name.orEmpty(),
                viaPlaceId = via?.placeId.orEmpty(),
                viaLatitude = via?.latitude,
                viaLongitude = via?.longitude,
            ),
        )
    }

    private fun toggleSave() {
        val destination = _uiState.value.destination ?: return
        viewModelScope.launch {
            when (
                savedPlacesRepository.toggleSaved(
                    SavePlaceRequest(
                        placeId = destination.placeId.ifBlank { null },
                        name = destination.name,
                        address = destination.address.ifBlank { null },
                        latitude = destination.latitude,
                        longitude = destination.longitude,
                    ),
                )
            ) {
                is ToggleSavedResult.Saved -> _uiState.update { it.copy(isSaved = true) }
                is ToggleSavedResult.Removed -> _uiState.update { it.copy(isSaved = false) }
            }
        }
    }

    private fun observeSaved(place: VoicePlace) {
        viewModelScope.launch {
            savedPlacesRepository.observeIsSaved(
                placeId = place.placeId.ifBlank { null },
                latitude = place.latitude,
                longitude = place.longitude,
            ).collect { saved ->
                _uiState.update { it.copy(isSaved = saved) }
            }
        }
    }

    private companion object {
        const val MAX_ALTERNATIVES = 5
    }
}
