package com.example.gpstracker.feature.compass.ui

enum class CompassTheme {
    Classic,
    Night,
    Terrain,
    Nautical,
}

data class CompassUiState(
    val headingDegrees: Float? = null,
    val cardinal: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val theme: CompassTheme = CompassTheme.Classic,
    val locating: Boolean = false,
)

sealed interface CompassEvent {
    data class ThemeSelected(val theme: CompassTheme) : CompassEvent
    data object RefreshLocationClicked : CompassEvent
}
