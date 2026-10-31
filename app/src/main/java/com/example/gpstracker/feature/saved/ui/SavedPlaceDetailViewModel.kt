package com.example.gpstracker.feature.saved.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedPlacesRepository: SavedPlacesRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.SavedPlaceDetail>()

    private val effectBus = EventBus<SavedPlaceDetailEffect>()
    val effects: Flow<SavedPlaceDetailEffect> = effectBus.events

    val uiState: StateFlow<SavedPlaceDetailUiState> =
        savedPlacesRepository.observePlace(route.placeId)
            .map { place ->
                SavedPlaceDetailUiState(place = place, isLoading = false)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SavedPlaceDetailUiState(),
            )

    fun onEvent(event: SavedPlaceDetailEvent) {
        when (event) {
            SavedPlaceDetailEvent.StartTripClicked -> {
                val place = uiState.value.place ?: return
                effectBus.tryEmit(
                    SavedPlaceDetailEffect.OpenRoutePreview(
                        destName = place.name,
                        destLatitude = place.latitude,
                        destLongitude = place.longitude,
                        destAddress = place.address.orEmpty(),
                        destPlaceId = place.placeId.orEmpty(),
                    ),
                )
            }
            SavedPlaceDetailEvent.UnsaveClicked -> {
                viewModelScope.launch {
                    savedPlacesRepository.remove(route.placeId)
                    effectBus.tryEmit(SavedPlaceDetailEffect.Close)
                }
            }
        }
    }
}
