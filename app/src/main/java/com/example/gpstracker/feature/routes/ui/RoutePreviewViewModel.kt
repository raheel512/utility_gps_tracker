package com.example.gpstracker.feature.routes.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.DistanceUnit
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
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.navigation.hasVia
import com.example.gpstracker.core.places.AutocompleteSessionManager
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.core.routes.GeoUri
import com.example.gpstracker.core.routes.RouteComputeRequest
import com.example.gpstracker.core.routes.RoutePlan
import com.example.gpstracker.core.routes.RoutesRepository
import com.example.gpstracker.core.routes.RoutesResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class RoutePreviewViewModel(
    private val args: Route.RoutePreview,
    private val routesRepository: RoutesRepository,
    private val historyRepository: RouteHistoryRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val placesRepository: PlacesRepository,
    private val sessionManager: AutocompleteSessionManager,
    private val reverseGeocoder: ReverseGeocoder,
    private val clock: Clock,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        routesRepository: RoutesRepository,
        historyRepository: RouteHistoryRepository,
        locationRepository: LocationRepository,
        settingsRepository: SettingsRepository,
        savedPlacesRepository: SavedPlacesRepository,
        connectivityRepository: ConnectivityRepository,
        placesRepository: PlacesRepository,
        sessionManager: AutocompleteSessionManager,
        reverseGeocoder: ReverseGeocoder,
        clock: Clock,
    ) : this(
        args = savedStateHandle.toRoute(),
        routesRepository = routesRepository,
        historyRepository = historyRepository,
        locationRepository = locationRepository,
        settingsRepository = settingsRepository,
        savedPlacesRepository = savedPlacesRepository,
        connectivityRepository = connectivityRepository,
        placesRepository = placesRepository,
        sessionManager = sessionManager,
        reverseGeocoder = reverseGeocoder,
        clock = clock,
    )

    // Must initialize before init { compute() } so History View Route includes via.
    private var viaPlaceId: String? = args.viaPlaceId.ifBlank { null }
    private var viaLatitude: Double? = if (args.hasVia()) args.viaLatitude else null
    private var viaLongitude: Double? = if (args.hasVia()) args.viaLongitude else null

    private val addStopQuery = MutableStateFlow("")
    private var searchJob: Job? = null
    private var biasLocation: LatLngPoint? = null

    private val _uiState = MutableStateFlow(
        RoutePreviewUiState(
            destName = args.destName,
            destAddress = args.destAddress,
            destPlaceId = args.destPlaceId,
            destLatitude = args.destLatitude,
            destLongitude = args.destLongitude,
            viaLabel = args.viaName.takeIf { it.isNotBlank() },
        ),
    )
    val uiState: StateFlow<RoutePreviewUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<RoutePreviewEffect>()
    val effects: Flow<RoutePreviewEffect> = effectBus.events

    init {
        observeSaved()
        observeAddStopQuery()
        compute()
    }

    fun onEvent(event: RoutePreviewEvent) {
        when (event) {
            RoutePreviewEvent.Refresh -> compute()
            RoutePreviewEvent.StartNavigationClicked -> startNavigation()
            RoutePreviewEvent.SaveClicked -> toggleSave()
            RoutePreviewEvent.AddStopClicked -> {
                addStopQuery.value = ""
                _uiState.update { it.copy(addStopVisible = true, suggestions = emptyList()) }
            }
            RoutePreviewEvent.DismissAddStop -> {
                _uiState.update { it.copy(addStopVisible = false, addStopQuery = "", suggestions = emptyList()) }
                sessionManager.reset()
            }
            is RoutePreviewEvent.AddStopQueryChanged -> addStopQuery.value = event.query
            is RoutePreviewEvent.ViaSuggestionSelected -> selectVia(event.suggestion)
            RoutePreviewEvent.StartLaunched -> recordHistory()
            RoutePreviewEvent.StartFailed -> {
                _uiState.update { it.copy(snackbarMessageRes = R.string.routes_maps_not_installed) }
            }
            RoutePreviewEvent.DismissNoInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
            }
            RoutePreviewEvent.ConnectInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
                effectBus.tryEmit(RoutePreviewEffect.OpenWirelessSettings)
            }
            RoutePreviewEvent.DismissError -> _uiState.update { it.copy(errorMessageRes = null) }
            RoutePreviewEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessageRes = null) }
            RoutePreviewEvent.ScreenStopped -> {
                searchJob?.cancel()
                sessionManager.reset()
            }
        }
    }

    private fun compute() {
        viewModelScope.launch {
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update {
                    it.copy(isLoading = false, showNoInternetDialog = true, canStart = false)
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            val fix = locationRepository.getCurrentLocation()
            if (fix == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.routes_location_unavailable,
                        canStart = false,
                    )
                }
                return@launch
            }
            biasLocation = LatLngPoint(fix.latitude, fix.longitude)
            val originLabel = reverseGeocoder.reverseGeocode(fix.latitude, fix.longitude)
                ?.locality
                ?: CoordinateFormatter.format(fix.latitude, fix.longitude)
            val avoidTolls = settingsRepository.avoidTolls.first()
            val avoidFerries = settingsRepository.avoidFerries.first()
            val unit = settingsRepository.distanceUnit.first()
            val viaName = _uiState.value.viaLabel
            val request = RouteComputeRequest(
                originLatitude = fix.latitude,
                originLongitude = fix.longitude,
                destName = args.destName,
                destAddress = args.destAddress.ifBlank { null },
                destPlaceId = args.destPlaceId.ifBlank { null },
                destLatitude = args.destLatitude,
                destLongitude = args.destLongitude,
                viaName = viaName,
                viaPlaceId = currentViaPlaceId(),
                viaLatitude = currentViaLatitude(),
                viaLongitude = currentViaLongitude(),
                avoidTolls = avoidTolls,
                avoidFerries = avoidFerries,
            )
            when (val result = routesRepository.computeRoute(request)) {
                is RoutesResult.Ok -> applyPlan(result.plan, originLabel, fix.latitude, fix.longitude, unit, avoidTolls, avoidFerries)
                RoutesResult.Empty -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plan = null,
                            canStart = false,
                            originLabel = originLabel,
                            originLatitude = fix.latitude,
                            originLongitude = fix.longitude,
                            avoidTolls = avoidTolls,
                            avoidFerries = avoidFerries,
                            errorMessageRes = R.string.routes_not_found,
                        )
                    }
                }
                RoutesResult.Offline -> {
                    _uiState.update {
                        it.copy(isLoading = false, showNoInternetDialog = true, canStart = false)
                    }
                }
                RoutesResult.Timeout, RoutesResult.Network, is RoutesResult.HttpError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            canStart = false,
                            errorMessageRes = R.string.routes_request_failed,
                        )
                    }
                }
            }
        }
    }

    private fun applyPlan(
        plan: RoutePlan,
        originLabel: String,
        originLat: Double,
        originLng: Double,
        unit: DistanceUnit,
        avoidTolls: Boolean,
        avoidFerries: Boolean,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                plan = plan,
                duration = DurationFormatter.display(plan.durationSeconds),
                distance = DistanceFormatter.format(plan.distanceMeters.toDouble(), unit),
                originLabel = originLabel,
                originLatitude = originLat,
                originLongitude = originLng,
                canStart = true,
                avoidTolls = avoidTolls,
                avoidFerries = avoidFerries,
                viaLabel = plan.viaLabel ?: it.viaLabel,
            )
        }
    }

    private fun startNavigation() {
        val state = _uiState.value
        if (!state.canStart) return
        val url = GeoUri.pin(
            latitude = state.destLatitude,
            longitude = state.destLongitude,
            label = state.destName,
        )
        effectBus.tryEmit(RoutePreviewEffect.OpenMaps(url))
    }

    private fun recordHistory() {
        val state = _uiState.value
        val plan = state.plan ?: return
        viewModelScope.launch {
            historyRepository.record(
                NewRouteHistory(
                    originLabel = state.originLabel.orEmpty().ifBlank { state.destName },
                    destLabel = state.destName,
                    destPlaceId = state.destPlaceId.ifBlank { null },
                    destLatitude = state.destLatitude,
                    destLongitude = state.destLongitude,
                    viaLabel = state.viaLabel,
                    viaPlaceId = currentViaPlaceId(),
                    viaLatitude = currentViaLatitude(),
                    viaLongitude = currentViaLongitude(),
                    plannedDurationSeconds = plan.durationSeconds,
                    plannedDistanceMeters = plan.distanceMeters,
                    encodedPolyline = plan.encodedPolyline,
                    startedAtMillis = clock.millis(),
                ),
            )
        }
    }

    private fun toggleSave() {
        val state = _uiState.value
        viewModelScope.launch {
            when (
                savedPlacesRepository.toggleSaved(
                    SavePlaceRequest(
                        placeId = state.destPlaceId.ifBlank { null },
                        name = state.destName,
                        address = state.destAddress.ifBlank { null },
                        latitude = state.destLatitude,
                        longitude = state.destLongitude,
                    ),
                )
            ) {
                is ToggleSavedResult.Saved -> _uiState.update { it.copy(isSaved = true) }
                is ToggleSavedResult.Removed -> _uiState.update { it.copy(isSaved = false) }
            }
        }
    }

    private fun observeSaved() {
        viewModelScope.launch {
            savedPlacesRepository.observeIsSaved(
                placeId = args.destPlaceId.ifBlank { null },
                latitude = args.destLatitude,
                longitude = args.destLongitude,
            ).collect { saved ->
                _uiState.update { it.copy(isSaved = saved) }
            }
        }
    }

    private fun observeAddStopQuery() {
        viewModelScope.launch {
            addStopQuery
                .debounce(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.update { it.copy(addStopQuery = query) }
                    searchVia(query)
                }
        }
    }

    private fun searchVia(query: String) {
        searchJob?.cancel()
        if (query.length < PlacesRepository.AUTOCOMPLETE_MIN_CHARS) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (
                val result = placesRepository.findAutocompletePredictions(
                    query = query,
                    session = sessionManager.current(),
                    locationBias = biasLocation,
                )
            ) {
                is PlacesResult.Ok -> {
                    _uiState.update { it.copy(suggestions = result.value, isSearching = false) }
                }
                is PlacesResult.Failure -> {
                    _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
                }
            }
        }
    }

    private fun selectVia(suggestion: PlaceSuggestion) {
        viewModelScope.launch {
            when (
                val result = placesRepository.fetchPlaceDetails(
                    placeId = suggestion.placeId,
                    displayName = suggestion.primaryText,
                    session = sessionManager.current(),
                )
            ) {
                is PlacesResult.Ok -> {
                    sessionManager.reset()
                    viaPlaceId = result.value.placeId
                    viaLatitude = result.value.latitude
                    viaLongitude = result.value.longitude
                    _uiState.update {
                        it.copy(
                            viaLabel = result.value.name,
                            addStopVisible = false,
                            addStopQuery = "",
                            suggestions = emptyList(),
                        )
                    }
                    compute()
                }
                is PlacesResult.Failure -> {
                    _uiState.update { it.copy(errorMessageRes = R.string.places_error_generic) }
                }
            }
        }
    }

    private fun currentViaPlaceId(): String? = viaPlaceId
    private fun currentViaLatitude(): Double? = viaLatitude
    private fun currentViaLongitude(): Double? = viaLongitude
}
