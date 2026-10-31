package com.example.gpstracker.feature.places.nearby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.data.saved.SavePlaceRequest
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.data.saved.ToggleSavedResult
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.NearbyCategory
import com.example.gpstracker.core.places.PlacesFailureReason
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.places.haversineMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placesRepository: PlacesRepository,
    private val savedPlacesRepository: SavedPlacesRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
    private val connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.NearbyResults>()
    private val category: NearbyCategory? = NearbyCategory.fromId(route.categoryId)

    private val _uiState = MutableStateFlow(
        NearbyResultsUiState(category = category, isLoading = true),
    )
    val uiState: StateFlow<NearbyResultsUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<NearbyResultsEffect>()
    val effects: Flow<NearbyResultsEffect> = effectBus.events

    private var searchJob: Job? = null
    private var savedObserveJob: Job? = null

    init {
        load()
    }

    fun onEvent(event: NearbyResultsEvent) {
        when (event) {
            is NearbyResultsEvent.PlaceClicked -> {
                _uiState.update { it.copy(selectedPlaceId = event.placeId) }
                observeSelectedSaved(event.placeId)
            }
            NearbyResultsEvent.Retry -> load()
            NearbyResultsEvent.DismissNoInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
            }
            NearbyResultsEvent.ConnectInternet -> {
                _uiState.update { it.copy(showNoInternetDialog = false) }
                effectBus.tryEmit(NearbyResultsEffect.OpenWirelessSettings)
            }
            NearbyResultsEvent.DismissError -> {
                _uiState.update { it.copy(errorMessageRes = null) }
            }
            NearbyResultsEvent.SaveClicked -> toggleSaveSelected()
            NearbyResultsEvent.DirectionsClicked -> {
                val placeId = _uiState.value.selectedPlaceId ?: return
                val place = _uiState.value.places.firstOrNull { it.place.placeId == placeId }?.place
                    ?: return
                effectBus.tryEmit(
                    NearbyResultsEffect.OpenRoutePreview(
                        destName = place.name,
                        destLatitude = place.latitude,
                        destLongitude = place.longitude,
                        destAddress = place.formattedAddress.orEmpty(),
                        destPlaceId = place.placeId,
                    ),
                )
            }
            NearbyResultsEvent.ScreenStopped -> {
                searchJob?.cancel()
                savedObserveJob?.cancel()
            }
        }
    }

    override fun onCleared() {
        searchJob?.cancel()
        super.onCleared()
    }

    private fun load() {
        val cat = category ?: run {
            _uiState.update {
                it.copy(isLoading = false, errorMessageRes = R.string.places_error_generic)
            }
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!connectivityRepository.isConnected.first()) {
                _uiState.update {
                    it.copy(isLoading = false, showNoInternetDialog = true)
                }
                return@launch
            }
            _uiState.update {
                it.copy(isLoading = true, errorMessageRes = null, emptyResults = false)
            }
            val fix = locationRepository.getCurrentLocation()
            if (fix == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.places_error_generic,
                    )
                }
                return@launch
            }
            val unit = settingsRepository.distanceUnit.first()
            val center = LatLngPoint(fix.latitude, fix.longitude)
            when (
                val result = placesRepository.searchNearby(
                    includedType = cat.id,
                    center = center,
                )
            ) {
                is PlacesResult.Ok -> {
                    val rows = result.value.map { place ->
                        val meters = haversineMeters(
                            fix.latitude,
                            fix.longitude,
                            place.latitude,
                            place.longitude,
                        )
                        NearbyPlaceRow(
                            place = place,
                            distanceMeters = meters,
                            distanceDisplay = DistanceFormatter.format(meters, unit),
                        )
                    }
                    _uiState.update {
                        it.copy(
                            places = rows,
                            isLoading = false,
                            emptyResults = rows.isEmpty(),
                            distanceUnit = unit,
                            userLatitude = fix.latitude,
                            userLongitude = fix.longitude,
                            selectedPlaceId = rows.firstOrNull()?.place?.placeId,
                        )
                    }
                    rows.firstOrNull()?.place?.placeId?.let { observeSelectedSaved(it) }
                }
                is PlacesResult.Failure -> handleFailure(result.reason)
            }
        }
    }

    private fun observeSelectedSaved(placeId: String) {
        val place = _uiState.value.places.firstOrNull { it.place.placeId == placeId }?.place
            ?: return
        savedObserveJob?.cancel()
        savedObserveJob = viewModelScope.launch {
            savedPlacesRepository.observeIsSaved(
                placeId = place.placeId,
                latitude = place.latitude,
                longitude = place.longitude,
            ).collect { saved ->
                _uiState.update { it.copy(isSelectedSaved = saved) }
            }
        }
    }

    private fun toggleSaveSelected() {
        val placeId = _uiState.value.selectedPlaceId ?: return
        val place = _uiState.value.places.firstOrNull { it.place.placeId == placeId }?.place
            ?: return
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
                    it.copy(isLoading = false, showNoInternetDialog = true)
                }
            }
            PlacesFailureReason.Empty -> {
                _uiState.update {
                    it.copy(isLoading = false, places = emptyList(), emptyResults = true)
                }
            }
            PlacesFailureReason.NotInitialized,
            PlacesFailureReason.ApiKeyOrApiDisabled,
            PlacesFailureReason.Unknown,
            -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.places_error_generic,
                    )
                }
            }
        }
    }
}
