package com.example.gpstracker.feature.weather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.feature.weather.WeatherFormat
import com.example.gpstracker.feature.weather.WeatherKind
import com.example.gpstracker.feature.weather.WeatherRepository
import com.example.gpstracker.feature.weather.WeatherSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val reverseGeocoder: ReverseGeocoder,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            WeatherEvent.RefreshClicked -> refresh()
        }
    }

    private fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true, failed = false) }
            val fix = locationRepository.getCurrentLocation()
            if (fix == null) {
                _uiState.update { it.copy(loading = false, failed = it.reading == null) }
                return@launch
            }
            val (address, snapshot) = coroutineScope {
                val place = async { reverseGeocoder.reverseGeocode(fix.latitude, fix.longitude) }
                val weather = async { weatherRepository.forecast(fix.latitude, fix.longitude) }
                place.await() to weather.await()
            }
            val reading = snapshot?.toReading()
            _uiState.update {
                it.copy(
                    loading = false,
                    failed = reading == null,
                    placeName = address?.placeLabel() ?: it.placeName,
                    reading = reading ?: it.reading,
                )
            }
        }
    }
}

private fun WeatherSnapshot.toReading(): WeatherReading? {
    val updated = WeatherFormat.clockLabel(observedAt) ?: return null
    val sunriseLabel = WeatherFormat.clockLabel(sunrise) ?: return null
    val sunsetLabel = WeatherFormat.clockLabel(sunset) ?: return null
    return WeatherReading(
        temperature = WeatherFormat.temperature(temperatureCelsius),
        kind = WeatherKind.fromCode(weatherCode),
        wind = WeatherFormat.windMetersPerSecond(windMps),
        humidityPercent = WeatherFormat.percent(humidityPercent),
        pressure = WeatherFormat.pressureHpa(pressureHpa),
        cloudPercent = WeatherFormat.percent(cloudPercent),
        sunrise = sunriseLabel,
        sunset = sunsetLabel,
        updatedAt = updated,
    )
}
