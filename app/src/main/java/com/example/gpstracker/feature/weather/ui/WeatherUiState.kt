package com.example.gpstracker.feature.weather.ui

import com.example.gpstracker.feature.weather.WeatherKind

data class WeatherUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val placeName: String? = null,
    val reading: WeatherReading? = null,
)

data class WeatherReading(
    val temperature: String,
    val kind: WeatherKind,
    val wind: String,
    val humidityPercent: Int,
    val pressure: String,
    val cloudPercent: Int,
    val sunrise: String,
    val sunset: String,
    val updatedAt: String,
)

sealed interface WeatherEvent {
    data object RefreshClicked : WeatherEvent
}
