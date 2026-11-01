package com.example.gpstracker.feature.emergency.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.format.AccuracyFormatter
import com.example.gpstracker.core.location.format.AccuracyUnit
import com.example.gpstracker.core.location.format.CoordinateFormatter
import com.example.gpstracker.core.location.geocode.GeocodedAddress
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.feature.emergency.DeviceCountryProvider
import com.example.gpstracker.feature.emergency.EmergencyNumberCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val reverseGeocoder: ReverseGeocoder,
    private val settingsRepository: SettingsRepository,
    private val deviceCountryProvider: DeviceCountryProvider,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<EmergencyEffect>()
    val effects = effectBus.events

    private var loadJob: Job? = null
    private var lastFix: UserLocation? = null
    private var lastAddress: GeocodedAddress? = null

    init {
        applyNumbers(deviceCountryProvider.isoHint())
        refresh()
    }

    fun onEvent(event: EmergencyEvent) {
        when (event) {
            EmergencyEvent.Refresh -> refresh()
            EmergencyEvent.DialGeneral -> dial(_uiState.value.generalNumber ?: _uiState.value.policeNumber)
            EmergencyEvent.DialPolice -> dial(_uiState.value.policeNumber)
            EmergencyEvent.DialAmbulance -> dial(_uiState.value.ambulanceNumber)
            EmergencyEvent.DialFire -> dial(_uiState.value.fireNumber)
            EmergencyEvent.ShareLocation -> shareLocation()
            EmergencyEvent.CopyLocation -> copyLocation()
            is EmergencyEvent.OpenNearby -> {
                viewModelScope.launch {
                    effectBus.emit(EmergencyEffect.OpenMapsQuery(event.kind.mapsQuery))
                }
            }
        }
    }

    private fun dial(number: String) {
        val cleaned = number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (cleaned.isBlank()) return
        viewModelScope.launch { effectBus.emit(EmergencyEffect.Dial(cleaned)) }
    }

    private fun shareLocation() {
        val fix = lastFix ?: return
        val state = _uiState.value
        val maps = String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", fix.latitude, fix.longitude)
        viewModelScope.launch {
            effectBus.emit(
                EmergencyEffect.ShareLocation(
                    placeLine = state.placeLine,
                    coordinates = state.coordinates,
                    mapsUrl = maps,
                ),
            )
        }
    }

    private fun copyLocation() {
        val state = _uiState.value
        val text = listOfNotNull(state.placeLine, state.coordinates).joinToString("\n")
        if (text.isBlank()) return
        viewModelScope.launch { effectBus.emit(EmergencyEffect.CopyText(text)) }
    }

    private fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val fix = withContext(dispatchers.io) {
                runCatching { locationRepository.getCurrentLocation() }.getOrNull()
            }
            if (fix == null) {
                applyNumbers(deviceCountryProvider.isoHint())
                _uiState.update {
                    it.copy(
                        loading = false,
                        hasFix = false,
                        placeLine = null,
                        coordinates = null,
                        accuracyLabel = null,
                    )
                }
                return@launch
            }
            lastFix = fix
            val unit = runCatching { settingsRepository.distanceUnit.first() }.getOrNull()
            val address = withContext(dispatchers.io) {
                runCatching { reverseGeocoder.reverseGeocode(fix.latitude, fix.longitude) }.getOrNull()
            }
            lastAddress = address
            val iso = address?.countryCode
                ?: deviceCountryProvider.isoHint()
            applyNumbers(iso, address?.countryName)
            _uiState.update {
                it.copy(
                    loading = false,
                    hasFix = true,
                    placeLine = address?.displayLine()
                        ?: address?.placeLabel(),
                    coordinates = CoordinateFormatter.format(fix.latitude, fix.longitude),
                    accuracyLabel = unit?.let { u ->
                        val display = AccuracyFormatter.toDisplay(fix.accuracyMeters, u)
                        val suffix = when (display.unit) {
                            AccuracyUnit.Meters -> "m"
                            AccuracyUnit.Feet -> "ft"
                        }
                        "${display.value} $suffix"
                    },
                )
            }
        }
    }

    private fun applyNumbers(iso: String?, geocodedCountryName: String? = null) {
        val numbers = EmergencyNumberCatalog.resolve(iso)
        val name = geocodedCountryName?.takeIf { it.isNotBlank() }
            ?: EmergencyNumberCatalog.countryDisplayName(numbers.iso.takeIf { it.isNotEmpty() } ?: iso)
        _uiState.update {
            it.copy(
                countryIso = numbers.iso.takeIf { code -> code.isNotEmpty() } ?: iso?.uppercase(),
                countryName = name,
                policeNumber = numbers.police,
                ambulanceNumber = numbers.ambulance,
                fireNumber = numbers.fire,
                generalNumber = numbers.general,
            )
        }
    }
}
