package com.example.gpstracker.feature.emergency.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.gpstracker.R
import com.example.gpstracker.core.places.NearbyCategory

data class EmergencyUiState(
    val loading: Boolean = true,
    val countryIso: String? = null,
    val countryName: String? = null,
    val placeLine: String? = null,
    val coordinates: String? = null,
    val accuracyLabel: String? = null,
    val hasFix: Boolean = false,
    val policeNumber: String = "112",
    val ambulanceNumber: String = "112",
    val fireNumber: String = "112",
    val generalNumber: String? = "112",
)

sealed interface EmergencyEvent {
    data object Refresh : EmergencyEvent
    data object DialGeneral : EmergencyEvent
    data object DialPolice : EmergencyEvent
    data object DialAmbulance : EmergencyEvent
    data object DialFire : EmergencyEvent
    data object ShareLocation : EmergencyEvent
    data object CopyLocation : EmergencyEvent
    data class OpenNearby(val kind: EmergencyNearbyKind) : EmergencyEvent
}

sealed interface EmergencyEffect {
    data class Dial(val number: String) : EmergencyEffect
    data class ShareLocation(
        val placeLine: String?,
        val coordinates: String?,
        val mapsUrl: String,
    ) : EmergencyEffect
    data class CopyText(val text: String) : EmergencyEffect
    data class OpenMapsQuery(val query: String) : EmergencyEffect
}

enum class EmergencyNearbyKind(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val mapsQuery: String,
    val category: NearbyCategory? = null,
) {
    Hospital(
        labelRes = R.string.nearby_cat_hospital,
        iconRes = R.drawable.ic_nearby_hospital,
        mapsQuery = "hospital",
        category = NearbyCategory.Hospital,
    ),
    Police(
        labelRes = R.string.nearby_cat_police,
        iconRes = R.drawable.ic_nearby_police,
        mapsQuery = "police",
        category = NearbyCategory.PoliceStation,
    ),
    Pharmacy(
        labelRes = R.string.emergency_nearby_pharmacy,
        iconRes = R.drawable.ic_nearby_doctor,
        mapsQuery = "pharmacy",
    ),
    Petrol(
        labelRes = R.string.nearby_cat_petrol_pump,
        iconRes = R.drawable.ic_nearby_petrol,
        mapsQuery = "petrol pump",
        category = NearbyCategory.PetrolPump,
    ),
}
