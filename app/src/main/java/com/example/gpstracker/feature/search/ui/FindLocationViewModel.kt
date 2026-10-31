package com.example.gpstracker.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.data.saved.SavePlaceRequest
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.data.saved.ToggleSavedResult
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.core.places.AutocompleteSessionManager
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlacesFailureReason
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.places.RecentSearchRepository
import com.example.gpstracker.feature.map.mylocation.buildLocationLine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindLocationViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val locationRepository: LocationRepository,
    private val reverseGeocoder: ReverseGeocoder,
    private val connectivityRepository: ConnectivityRepository,
    private val sessionManager: AutocompleteSessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindLocationUiState())
    val uiState: StateFlow<FindLocationUiState> = _uiState.asStateFlow()

    private val destinationQuery = MutableStateFlow("")
    private var biasLocation: LatLngPoint? = null
    private var searchJob: Job? = null

    private val effectBus = EventBus<FindLocationEffect>()
    val effects: Flow<FindLocationEffect> = effectBus.events

    private var gpsJob: Job? = null

    init {
        refreshFromGps(reportFailure = false)

        viewModelScope.launch {
            destinationQuery
                .debounce(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS)
                .map { it.trim() }
                .distinctUntilChanged()
                .collect { query -> runAutocomplete(query) }
        }
    }

    fun onEvent(event: FindLocationEvent) {
        when (event) {
            is FindLocationEvent.VoicePlaceChosen -> applyVoicePlace(event)
            is FindLocationEvent.DestinationQueryChanged -> {
                _uiState.update {
                    it.copy(
                        selectedPlace = null,
                        emptyResults = false,
                        errorMessageRes = null,
                        destinationQuery = event.query,
                    )
                }
                destinationQuery.value = event.query
            }
            is FindLocationEvent.SuggestionSelected -> selectSuggestion(event.suggestion)
            FindLocationEvent.SwapClicked -> swapFields()
            FindLocationEvent.UseGpsClicked -> refreshFromGps(reportFailure = true)
            FindLocationEvent.VoiceSearchClicked -> {
                effectBus.tryEmit(FindLocationEffect.OpenVoice)
            }
            FindLocationEvent.NavigateClicked -> {
                val place = _uiState.value.selectedPlace
                if (place == null) {
                    _uiState.update { it.copy(errorMessageRes = R.string.routes_choose_destination) }
                } else {
                    effectBus.tryEmit(
                        FindLocationEffect.OpenRoutePreview(
                            destName = place.name,
                            destLatitude = place.latitude,
                            destLongitude = place.longitude,
                            destAddress = place.formattedAddress.orEmpty(),
                            destPlaceId = place.placeId,
                        ),
                    )
                }
            }
            FindLocationEvent.SaveClicked -> toggleSaveSelected()
            FindLocationEvent.ClearSelection -> {
                _uiState.update { it.copy(selectedPlace = null) }
            }
            FindLocationEvent.DismissNoInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
            }
            FindLocationEvent.ConnectInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
                effectBus.tryEmit(FindLocationEffect.OpenWirelessSettings)
            }
            FindLocationEvent.DismissError -> {
                _uiState.update { it.copy(errorMessageRes = null) }
            }
            FindLocationEvent.ScreenStopped -> {
                searchJob?.cancel()
                sessionManager.reset()
            }
        }
    }

    override fun onCleared() {
        searchJob?.cancel()
        sessionManager.reset()
        super.onCleared()
    }

    private fun runAutocomplete(query: String) {
        searchJob?.cancel()
        if (query.length < PlacesRepository.AUTOCOMPLETE_MIN_CHARS) {
            _uiState.update {
                it.copy(suggestions = emptyList(), isSearching = false, emptyResults = false)
            }
            return
        }
        searchJob = viewModelScope.launch {
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update {
                    it.copy(
                        showNoInternetDialog = true,
                        suggestions = emptyList(),
                        isSearching = false,
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(isSearching = true, emptyResults = false, errorMessageRes = null)
            }
            when (
                val result = placesRepository.findAutocompletePredictions(
                    query = query,
                    session = sessionManager.current(),
                    locationBias = biasLocation,
                )
            ) {
                is PlacesResult.Ok -> {
                    _uiState.update {
                        it.copy(
                            suggestions = result.value,
                            emptyResults = result.value.isEmpty(),
                            isSearching = false,
                        )
                    }
                }
                is PlacesResult.Failure -> {
                    handleFailure(result.reason)
                    _uiState.update { it.copy(isSearching = false) }
                }
            }
        }
    }

    private fun applyVoicePlace(event: FindLocationEvent.VoicePlaceChosen) {
        searchJob?.cancel()
        val details = PlaceDetails(
            placeId = event.placeId,
            name = event.name,
            formattedAddress = event.address.ifBlank { null },
            latitude = event.latitude,
            longitude = event.longitude,
        )
        _uiState.update {
            it.copy(
                selectedPlace = details,
                destinationQuery = event.name,
                suggestions = emptyList(),
                emptyResults = false,
                isSearching = false,
                errorMessageRes = null,
            )
        }
        viewModelScope.launch {
            recentSearchRepository.saveFromDetails(
                details = details,
                secondaryText = event.address,
            )
        }
        observeSavedState(event.placeId, event.latitude, event.longitude)
    }

    private fun selectSuggestion(suggestion: PlaceSuggestion) {
        searchJob?.cancel()
        viewModelScope.launch {
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update { it.copy(showNoInternetDialog = true) }
                return@launch
            }
            _uiState.update { it.copy(isSearching = true) }
            when (
                val result = placesRepository.fetchPlaceDetails(
                    placeId = suggestion.placeId,
                    displayName = suggestion.primaryText,
                    session = sessionManager.current(),
                )
            ) {
                is PlacesResult.Ok -> {
                    val details = result.value
                    val isSaved = savedPlacesRepository.observeIsSaved(
                        placeId = details.placeId,
                        latitude = details.latitude,
                        longitude = details.longitude,
                    ).first()
                    _uiState.update {
                        it.copy(
                            selectedPlace = details,
                            isSelectedSaved = isSaved,
                            destinationQuery = suggestion.primaryText,
                            suggestions = emptyList(),
                            emptyResults = false,
                            isSearching = false,
                        )
                    }
                    destinationQuery.value = suggestion.primaryText
                    recentSearchRepository.saveFromDetails(
                        details = details,
                        secondaryText = suggestion.secondaryText,
                    )
                    sessionManager.reset()
                    observeSavedState(details.placeId, details.latitude, details.longitude)
                }
                is PlacesResult.Failure -> {
                    handleFailure(result.reason)
                    _uiState.update { it.copy(isSearching = false) }
                }
            }
        }
    }

    private fun refreshFromGps(reportFailure: Boolean) {
        gpsJob?.cancel()
        gpsJob = viewModelScope.launch {
            _uiState.update { it.copy(locating = true) }
            val fix = locationRepository.getCurrentLocation()
            if (fix == null) {
                _uiState.update {
                    it.copy(
                        locating = false,
                        errorMessageRes = if (reportFailure) {
                            R.string.find_location_gps_unavailable
                        } else {
                            it.errorMessageRes
                        },
                    )
                }
                return@launch
            }
            biasLocation = LatLngPoint(fix.latitude, fix.longitude)
            val address = reverseGeocoder.reverseGeocode(fix.latitude, fix.longitude)
            _uiState.update {
                it.copy(
                    locating = false,
                    userLatitude = fix.latitude,
                    userLongitude = fix.longitude,
                    locationRevision = it.locationRevision + 1,
                    myLocationLine = buildLocationLine(fix, address),
                )
            }
        }
    }

    private fun swapFields() {
        val state = _uiState.value
        val dest = state.selectedPlace?.name ?: state.destinationQuery.trim()
        if (dest.isEmpty()) return
        val previousOrigin = state.myLocationLine.orEmpty()
        _uiState.update {
            it.copy(
                myLocationLine = dest,
                destinationQuery = previousOrigin,
                selectedPlace = null,
                suggestions = emptyList(),
            )
        }
        destinationQuery.value = previousOrigin
    }

    private var savedObserveJob: Job? = null

    private fun observeSavedState(placeId: String?, latitude: Double, longitude: Double) {
        savedObserveJob?.cancel()
        savedObserveJob = viewModelScope.launch {
            savedPlacesRepository.observeIsSaved(placeId, latitude, longitude).collect { saved ->
                _uiState.update { it.copy(isSelectedSaved = saved) }
            }
        }
    }

    private fun toggleSaveSelected() {
        val place = _uiState.value.selectedPlace ?: return
        viewModelScope.launch {
            when (
                savedPlacesRepository.toggleSaved(
                    SavePlaceRequest(
                        placeId = place.placeId,
                        name = place.name,
                        address = place.formattedAddress,
                        latitude = place.latitude,
                        longitude = place.longitude,
                    ),
                )
            ) {
                is ToggleSavedResult.Saved -> _uiState.update { it.copy(isSelectedSaved = true) }
                is ToggleSavedResult.Removed -> _uiState.update { it.copy(isSelectedSaved = false) }
            }
        }
    }

    private fun handleFailure(reason: PlacesFailureReason) {
        when (reason) {
            PlacesFailureReason.Network -> {
                _uiState.update {
                    it.copy(suggestions = emptyList(), showNoInternetDialog = true)
                }
            }
            PlacesFailureReason.NotInitialized,
            PlacesFailureReason.ApiKeyOrApiDisabled,
            PlacesFailureReason.Unknown,
            -> {
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        errorMessageRes = R.string.places_error_generic,
                    )
                }
            }
            PlacesFailureReason.Empty -> {
                _uiState.update {
                    it.copy(suggestions = emptyList(), emptyResults = true)
                }
            }
        }
    }
}
