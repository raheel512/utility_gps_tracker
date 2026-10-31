package com.example.gpstracker.feature.map.mylocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.format.AccuracyFormatter
import com.example.gpstracker.core.location.format.CoordinateFormatter
import com.example.gpstracker.core.location.format.PlusCodeFormatter
import com.example.gpstracker.core.location.geocode.GeocodedAddress
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.core.map.AppMapType
import com.example.gpstracker.core.routes.GeoUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyLocationViewModel @Inject constructor(
    locationRepository: LocationRepository,
    private val reverseGeocoder: ReverseGeocoder,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mapType = MutableStateFlow(AppMapType.Satellite)
    private val effectBus = EventBus<MyLocationEffect>()
    val effects: Flow<MyLocationEffect> = effectBus.events

    /** Upstream GPS stops [STOP_TIMEOUT_MS] after the screen stops collecting. */
    private val locations = flow {
        locationRepository.getCurrentLocation()?.let { emit(it) }
        emitAll(locationRepository.locationUpdates())
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), replay = 1)

    private val address: Flow<GeocodedAddress?> = locations
        .distinctUntilChangedBy { it.roundedKey() }
        .mapLatest { reverseGeocoder.reverseGeocode(it.latitude, it.longitude) }
        .onStart { emit(null) }

    val uiState: StateFlow<MyLocationUiState> = combine(
        locations.onStart<UserLocation?> { emit(null) },
        address,
        settingsRepository.distanceUnit,
        mapType,
    ) { location, address, unit, type ->
        MyLocationUiState(
            location = location,
            locationLine = location?.let { buildLocationLine(it, address) },
            coordinates = location?.let { CoordinateFormatter.format(it.latitude, it.longitude) },
            accuracy = location?.let { AccuracyFormatter.toDisplay(it.accuracyMeters, unit) },
            mapType = type,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MyLocationUiState())

    fun onEvent(event: MyLocationEvent) {
        when (event) {
            MyLocationEvent.ToggleMapType -> mapType.update {
                if (it == AppMapType.Satellite) AppMapType.Normal else AppMapType.Satellite
            }
            MyLocationEvent.CopyClicked -> {
                val line = uiState.value.locationLine ?: return
                effectBus.tryEmit(MyLocationEffect.CopyToClipboard(line))
            }
            MyLocationEvent.ShareClicked -> {
                val state = uiState.value
                val location = state.location ?: return
                val line = state.locationLine ?: return
                effectBus.tryEmit(MyLocationEffect.ShareText(buildShareText(line, state.coordinates, location)))
            }
        }
    }

    private fun UserLocation.roundedKey(): Pair<Long, Long> =
        (latitude * GEOCODE_SCALE).roundToLong() to (longitude * GEOCODE_SCALE).roundToLong()

    companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        private const val GEOCODE_SCALE = 10_000.0
    }
}

internal fun buildLocationLine(location: UserLocation, address: GeocodedAddress?): String {
    val fullCode = PlusCodeFormatter.fullCode(location.latitude, location.longitude)
    val addressLine = address?.displayLine() ?: return fullCode
    val shortCode = PlusCodeFormatter.localityShortCode(fullCode, location.latitude, location.longitude)
        ?: return fullCode
    return "$shortCode, $addressLine"
}

internal fun mapsLink(location: UserLocation): String =
    GeoUri.pin(location.latitude, location.longitude)

internal fun buildShareText(line: String, coordinates: String?, location: UserLocation): String =
    listOfNotNull(line, coordinates, mapsLink(location)).joinToString(separator = "\n")
