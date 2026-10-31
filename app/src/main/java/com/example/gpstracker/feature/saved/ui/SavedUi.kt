package com.example.gpstracker.feature.saved.ui

import com.example.gpstracker.core.data.saved.PlaceCollection
import com.example.gpstracker.core.data.saved.SavedPlace
import com.example.gpstracker.core.data.saved.SavedPlaceIconKind
import com.example.gpstracker.core.places.DistanceFormatter

/** Stable stored label for a spot saved from the current GPS fix. Shown localized. */
internal const val SAVED_CURRENT_SPOT_NAME = "Current Location"

data class SavedListItemUi(
    val key: String,
    val place: SavedPlace? = null,
    val collection: PlaceCollection? = null,
    val title: String,
    val subtitle: String?,
    val distanceLabel: String?,
    val iconKind: SavedPlaceIconKind = SavedPlaceIconKind.PLACE,
    val spotCountLabel: String? = null,
)

data class SavedUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val searchVisible: Boolean = false,
    val currentLocationLine: String? = null,
    val accuracyMeters: Int? = null,
    val canSaveCurrent: Boolean = false,
    val items: List<SavedListItemUi> = emptyList(),
    val showCreateCollectionDialog: Boolean = false,
    val createCollectionName: String = "",
    val snackbarMessageRes: Int? = null,
    val pendingUndo: SavedPlace? = null,
)

sealed interface SavedEvent {
    data object RefreshLocation : SavedEvent
    data object SaveSpotClicked : SavedEvent
    data object SearchClicked : SavedEvent
    data object DismissSearch : SavedEvent
    data class QueryChanged(val query: String) : SavedEvent
    data object AddCollectionClicked : SavedEvent
    data object DismissCreateCollection : SavedEvent
    data class CreateCollectionNameChanged(val name: String) : SavedEvent
    data object ConfirmCreateCollection : SavedEvent
    data class PlaceClicked(val placeId: Long) : SavedEvent
    data class CollectionClicked(val collectionId: Long) : SavedEvent
    data class UnsaveClicked(val placeId: Long) : SavedEvent
    data class StartTripClicked(val placeId: Long) : SavedEvent
    data object UndoUnsave : SavedEvent
    data object SnackbarShown : SavedEvent
}

sealed interface SavedEffect {
    data class OpenPlace(val placeId: Long) : SavedEffect
    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destAddress: String,
        val destPlaceId: String,
    ) : SavedEffect
}
