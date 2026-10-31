package com.example.gpstracker.feature.map.mylocation

import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.format.AccuracyDisplay
import com.example.gpstracker.core.map.AppMapType

data class MyLocationUiState(
    val location: UserLocation? = null,
    /** Card headline: "M2XP+RFW, 9th Ave, F-9, Islamabad, Pakistan", or the full Plus Code without an address. */
    val locationLine: String? = null,
    val coordinates: String? = null,
    /** Not shown in the design; kept for share text and future use. */
    val accuracy: AccuracyDisplay? = null,
    val mapType: AppMapType = AppMapType.Satellite,
) {
    val canShareOrCopy: Boolean get() = locationLine != null
}

sealed interface MyLocationEvent {
    data object ToggleMapType : MyLocationEvent
    data object ShareClicked : MyLocationEvent
    data object CopyClicked : MyLocationEvent
}

sealed interface MyLocationEffect {
    data class CopyToClipboard(val text: String) : MyLocationEffect
    data class ShareText(val text: String) : MyLocationEffect
}
