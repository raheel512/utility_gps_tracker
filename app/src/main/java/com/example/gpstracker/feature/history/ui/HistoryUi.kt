package com.example.gpstracker.feature.history.ui

import com.example.gpstracker.core.data.history.RouteHistoryEntry
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.feature.history.HistoryRange

data class HistoryListItemUi(
    val entry: RouteHistoryEntry,
    val title: String,
    val startedLabel: String,
    val duration: DurationFormatter.Display?,
    val distance: DistanceFormatter.DistanceDisplay?,
)

data class HistoryDayGroupUi(
    val key: String,
    val isToday: Boolean,
    val isYesterday: Boolean,
    val dateLabel: String,
    val tripCount: Int,
    val items: List<HistoryListItemUi>,
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val range: HistoryRange = HistoryRange.All,
    val groups: List<HistoryDayGroupUi> = emptyList(),
    val isEmpty: Boolean = false,
)

sealed interface HistoryEvent {
    data class RangeSelected(val range: HistoryRange) : HistoryEvent
    data class ViewRouteClicked(val entryId: Long) : HistoryEvent
    data class DeleteClicked(val entryId: Long) : HistoryEvent
    data class MenuClicked(val entryId: Long) : HistoryEvent
    data object DismissMenu : HistoryEvent
}

data class HistoryMenuState(val entryId: Long? = null)

sealed interface HistoryEffect {
    data class OpenRoutePreview(
        val destName: String,
        val destLatitude: Double,
        val destLongitude: Double,
        val destPlaceId: String,
        val destAddress: String,
        val viaName: String,
        val viaPlaceId: String,
        val viaLatitude: Double?,
        val viaLongitude: Double?,
    ) : HistoryEffect
}
