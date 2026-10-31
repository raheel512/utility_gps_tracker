package com.example.gpstracker.core.places

import androidx.annotation.StringRes
import com.example.gpstracker.R

/**
 * Nearby Places categories from Figma `55505:2918` (CATEGORIES 21) plus Home tiles.
 * [osmKey] and [osmValue] select the OpenStreetMap tag used by Overpass.
 */
enum class NearbyCategory(
    val id: String,
    val osmKey: String,
    val osmValue: String,
    @param:StringRes val labelRes: Int,
    val osmExtraKey: String? = null,
    val osmExtraValue: String? = null,
) {
    Hospital("hospital", "amenity", "hospital", R.string.nearby_cat_hospital),
    Hotels("hotels", "tourism", "hotel", R.string.nearby_cat_hotels),
    Restaurant("restaurant", "amenity", "restaurant", R.string.nearby_cat_restaurant),
    Airport("airport", "aeroway", "aerodrome", R.string.nearby_cat_airport),
    Bank("bank", "amenity", "bank", R.string.nearby_cat_bank),
    Atm("atm", "amenity", "atm", R.string.nearby_cat_atm),
    PostOffice("post_office", "amenity", "post_office", R.string.nearby_cat_post_office),
    School("school", "amenity", "school", R.string.nearby_cat_school),
    University("university", "amenity", "university", R.string.nearby_cat_university),
    BusStation("bus_station", "amenity", "bus_station", R.string.nearby_cat_bus_station),
    PoliceStation("police", "amenity", "police", R.string.nearby_cat_police),
    Doctor("doctor", "amenity", "doctors", R.string.nearby_cat_doctor),
    Theater("theater", "amenity", "cinema", R.string.nearby_cat_theater),
    Shopping("shopping", "shop", "mall", R.string.nearby_cat_shopping),
    PetrolPump("petrol_pump", "amenity", "fuel", R.string.nearby_cat_petrol_pump),
    CcdCafe("ccd_cafe", "amenity", "cafe", R.string.nearby_cat_ccd_cafe),
    Mosques(
        "mosques",
        "amenity",
        "place_of_worship",
        R.string.nearby_cat_mosques,
        osmExtraKey = "religion",
        osmExtraValue = "muslim",
    ),
    Church(
        "church",
        "amenity",
        "place_of_worship",
        R.string.nearby_cat_church,
        osmExtraKey = "religion",
        osmExtraValue = "christian",
    ),
    Gym("gym", "leisure", "fitness_centre", R.string.nearby_cat_gym),
    Spa("spa", "leisure", "spa", R.string.nearby_cat_spa),
    Garage("garage", "shop", "car_repair", R.string.nearby_cat_garage),
    ;

    /** Words Google Maps should search. Labels that are not a place type are rewritten. */
    fun mapsSearchQuery(label: String): String = when (this) {
        CcdCafe -> "cafe"
        Theater -> "movie theater"
        Shopping -> "shopping mall"
        Garage -> "car repair"
        Spa -> "sport club"
        else -> label
    }

    companion object {
        /** Order matches Figma near places grid. */
        val seeMoreOrder: List<NearbyCategory> = entries.toList()

        fun fromId(id: String?): NearbyCategory? =
            id?.let { value -> entries.firstOrNull { it.id == value } }
    }
}

/** OSM tag value for a category id, or null when the id is unknown. */
fun osmValueForCategoryId(categoryId: String): String? =
    NearbyCategory.fromId(categoryId)?.osmValue
