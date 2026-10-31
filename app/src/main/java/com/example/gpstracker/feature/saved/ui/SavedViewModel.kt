package com.example.gpstracker.feature.saved.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.saved.PlaceCollection
import com.example.gpstracker.core.data.saved.SavePlaceRequest
import com.example.gpstracker.core.data.saved.SavedPlace
import com.example.gpstracker.core.data.saved.SavedPlaceIconKind
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.format.CoordinateFormatter
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.haversineMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedPlacesRepository: SavedPlacesRepository,
    private val locationRepository: LocationRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val searchVisible = MutableStateFlow(false)
    private val currentLocation = MutableStateFlow<UserLocation?>(null)
    private val showCreateCollectionDialog = MutableStateFlow(false)
    private val createCollectionName = MutableStateFlow("")
    private val snackbarMessageRes = MutableStateFlow<Int?>(null)
    private val pendingUndo = MutableStateFlow<SavedPlace?>(null)

    private val effectBus = EventBus<SavedEffect>()
    val effects: Flow<SavedEffect> = effectBus.events

    private data class DialogState(
        val show: Boolean,
        val name: String,
        val snackbarRes: Int?,
        val undo: SavedPlace?,
    )

    private data class FilterState(
        val query: String,
        val searchVisible: Boolean,
        val location: UserLocation?,
        val unit: DistanceUnit,
    )

    init {
        refreshLocation()
    }

    private val filterState = combine(
        query,
        searchVisible,
        currentLocation,
        settingsRepository.distanceUnit,
    ) { q, searching, location, unit ->
        FilterState(q, searching, location, unit)
    }

    private val dialogState = combine(
        showCreateCollectionDialog,
        createCollectionName,
        snackbarMessageRes,
        pendingUndo,
    ) { show, name, snackbar, undo ->
        DialogState(show, name, snackbar, undo)
    }

    val uiState: StateFlow<SavedUiState> = combine(
        savedPlacesRepository.observePlaces(),
        savedPlacesRepository.observeCollections(),
        filterState,
        dialogState,
    ) { places, collections, filter, dialog ->
        SavedUiState(
            isLoading = false,
            query = filter.query,
            searchVisible = filter.searchVisible,
            currentLocationLine = filter.location?.let { loc ->
                CoordinateFormatter.format(loc.latitude, loc.longitude)
            },
            accuracyMeters = filter.location?.accuracyMeters?.toInt(),
            canSaveCurrent = filter.location != null,
            items = buildListItems(
                places = places,
                collections = collections,
                location = filter.location,
                unit = filter.unit,
                query = filter.query,
            ),
            showCreateCollectionDialog = dialog.show,
            createCollectionName = dialog.name,
            snackbarMessageRes = dialog.snackbarRes,
            pendingUndo = dialog.undo,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun onEvent(event: SavedEvent) {
        when (event) {
            SavedEvent.RefreshLocation -> refreshLocation()
            SavedEvent.SaveSpotClicked -> saveCurrentSpot()
            SavedEvent.SearchClicked -> searchVisible.value = true
            SavedEvent.DismissSearch -> {
                searchVisible.value = false
                query.value = ""
            }
            is SavedEvent.QueryChanged -> query.value = event.query
            SavedEvent.AddCollectionClicked -> {
                createCollectionName.value = ""
                showCreateCollectionDialog.value = true
            }
            SavedEvent.DismissCreateCollection -> showCreateCollectionDialog.value = false
            is SavedEvent.CreateCollectionNameChanged -> createCollectionName.value = event.name
            SavedEvent.ConfirmCreateCollection -> createCollection()
            is SavedEvent.PlaceClicked -> effectBus.tryEmit(SavedEffect.OpenPlace(event.placeId))
            is SavedEvent.CollectionClicked -> Unit
            is SavedEvent.UnsaveClicked -> unsave(event.placeId)
            is SavedEvent.StartTripClicked -> {
                val place = uiState.value.items.firstOrNull { it.place?.id == event.placeId }?.place
                if (place != null) {
                    effectBus.tryEmit(
                        SavedEffect.OpenRoutePreview(
                            destName = place.name,
                            destLatitude = place.latitude,
                            destLongitude = place.longitude,
                            destAddress = place.address.orEmpty(),
                            destPlaceId = place.placeId.orEmpty(),
                        ),
                    )
                }
            }
            SavedEvent.UndoUnsave -> undoUnsave()
            SavedEvent.SnackbarShown -> snackbarMessageRes.value = null
        }
    }

    private fun refreshLocation() {
        viewModelScope.launch {
            currentLocation.value = locationRepository.getCurrentLocation()
        }
    }

    private fun saveCurrentSpot() {
        val location = currentLocation.value ?: return
        viewModelScope.launch {
            savedPlacesRepository.toggleSaved(
                SavePlaceRequest(
                    placeId = null,
                    name = SAVED_CURRENT_SPOT_NAME,
                    address = CoordinateFormatter.format(location.latitude, location.longitude),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    iconKind = SavedPlaceIconKind.PLACE,
                ),
            )
        }
    }

    private fun createCollection() {
        val name = createCollectionName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            savedPlacesRepository.createCollection(name)
            showCreateCollectionDialog.value = false
            createCollectionName.value = ""
        }
    }

    private fun unsave(placeId: Long) {
        viewModelScope.launch {
            val removed = savedPlacesRepository.remove(placeId) ?: return@launch
            pendingUndo.value = removed
            snackbarMessageRes.value = R.string.saved_removed_snackbar
        }
    }

    private fun undoUnsave() {
        val place = pendingUndo.value ?: return
        viewModelScope.launch {
            savedPlacesRepository.restore(place)
            pendingUndo.value = null
        }
    }

    companion object {
        internal fun buildListItems(
            places: List<SavedPlace>,
            collections: List<PlaceCollection>,
            location: UserLocation?,
            unit: DistanceUnit,
            query: String,
        ): List<SavedListItemUi> {
            val q = query.trim().lowercase()
            val placeItems = places
                .filter {
                    q.isEmpty() ||
                        it.name.lowercase().contains(q) ||
                        (it.address?.lowercase()?.contains(q) == true)
                }
                .map { place ->
                    val distance = location?.let {
                        val meters = haversineMeters(
                            it.latitude,
                            it.longitude,
                            place.latitude,
                            place.longitude,
                        )
                        formatDistance(DistanceFormatter.format(meters, unit))
                    }
                    SavedListItemUi(
                        key = "place-${place.id}",
                        place = place,
                        title = place.name,
                        subtitle = place.address,
                        distanceLabel = distance,
                        iconKind = place.iconKind,
                    )
                }
            val collectionItems = collections
                .filter { q.isEmpty() || it.name.lowercase().contains(q) }
                .map { collection ->
                    SavedListItemUi(
                        key = "collection-${collection.id}",
                        collection = collection,
                        title = collection.name,
                        subtitle = null,
                        distanceLabel = null,
                        iconKind = SavedPlaceIconKind.PLACE,
                        spotCountLabel = collection.placeCount.toString(),
                    )
                }
            // Collection rows follow places so the list matches Figma mixing.
            return placeItems + collectionItems
        }

        private fun formatDistance(display: DistanceFormatter.DistanceDisplay): String {
            val unit = when (display.unit) {
                DistanceFormatter.DistanceDisplayUnit.Km -> "km"
                DistanceFormatter.DistanceDisplayUnit.M -> "m"
                DistanceFormatter.DistanceDisplayUnit.Mi -> "mi"
                DistanceFormatter.DistanceDisplayUnit.Ft -> "ft"
            }
            return "${display.valueText} $unit"
        }
    }
}
