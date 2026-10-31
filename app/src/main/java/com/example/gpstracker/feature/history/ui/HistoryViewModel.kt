package com.example.gpstracker.feature.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.history.RouteHistoryEntry
import com.example.gpstracker.core.data.history.RouteHistoryRepository
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.feature.history.HistoryDateFilter
import com.example.gpstracker.feature.history.HistoryRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: RouteHistoryRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val range = MutableStateFlow(HistoryRange.Today)
    private val menuEntryId = MutableStateFlow<Long?>(null)

    private val effectBus = EventBus<HistoryEffect>()
    val effects: Flow<HistoryEffect> = effectBus.events

    val menuState: StateFlow<HistoryMenuState> = menuEntryId
        .combine(range) { id, _ -> HistoryMenuState(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryMenuState())

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.observeAll(),
        range,
        settingsRepository.distanceUnit,
    ) { entries, selectedRange, unit ->
        val filtered = entries.filter { entry ->
            HistoryDateFilter.startedAtInRange(entry.startedAtMillis, selectedRange, clock)
        }
        HistoryUiState(
            isLoading = false,
            range = selectedRange,
            groups = group(filtered, unit),
            isEmpty = filtered.isEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.RangeSelected -> range.value = event.range
            is HistoryEvent.ViewRouteClicked -> openRoute(event.entryId)
            is HistoryEvent.DeleteClicked -> {
                menuEntryId.value = null
                viewModelScope.launch { historyRepository.delete(event.entryId) }
            }
            is HistoryEvent.MenuClicked -> menuEntryId.value = event.entryId
            HistoryEvent.DismissMenu -> menuEntryId.value = null
        }
    }

    private fun openRoute(entryId: Long) {
        viewModelScope.launch {
            val entry = uiState.value.groups
                .flatMap { it.items }
                .firstOrNull { it.entry.id == entryId }
                ?.entry
                ?: return@launch
            effectBus.tryEmit(
                HistoryEffect.OpenRoutePreview(
                    destName = entry.destLabel,
                    destLatitude = entry.destLatitude,
                    destLongitude = entry.destLongitude,
                    destPlaceId = entry.destPlaceId.orEmpty(),
                    destAddress = "",
                    viaName = entry.viaLabel.orEmpty(),
                    viaPlaceId = entry.viaPlaceId.orEmpty(),
                    viaLatitude = entry.viaLatitude,
                    viaLongitude = entry.viaLongitude,
                ),
            )
        }
    }

    private fun group(
        entries: List<RouteHistoryEntry>,
        unit: DistanceUnit,
    ): List<HistoryDayGroupUi> {
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return entries
            .groupBy { Instant.ofEpochMilli(it.startedAtMillis).atZone(zone).toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .map { (date, dayEntries) ->
                HistoryDayGroupUi(
                    key = date.toString(),
                    isToday = date == today,
                    isYesterday = date == today.minusDays(1),
                    dateLabel = date.format(dateFormatter),
                    tripCount = dayEntries.size,
                    items = dayEntries.map { entry ->
                        HistoryListItemUi(
                            entry = entry,
                            title = "${entry.originLabel} → ${entry.destLabel}",
                            startedLabel = Instant.ofEpochMilli(entry.startedAtMillis)
                                .atZone(zone)
                                .format(timeFormatter),
                            duration = if (entry.hasRouteStats) {
                                DurationFormatter.display(entry.plannedDurationSeconds)
                            } else {
                                null
                            },
                            distance = if (entry.hasRouteStats) {
                                DistanceFormatter.format(
                                    entry.plannedDistanceMeters.toDouble(),
                                    unit,
                                )
                            } else {
                                null
                            },
                        )
                    },
                )
            }
    }
}
