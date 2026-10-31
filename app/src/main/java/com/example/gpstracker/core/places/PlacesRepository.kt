package com.example.gpstracker.core.places

/**
 * Place search and nearby lookup. Implementations must not expose provider types to callers.
 */
interface PlacesRepository {
    fun newAutocompleteSession(): AutocompleteSession

    suspend fun findAutocompletePredictions(
        query: String,
        session: AutocompleteSession,
        locationBias: LatLngPoint?,
    ): PlacesResult<List<PlaceSuggestion>>

    /**
     * Place Details Essentials fields only: id, location, formattedAddress.
     * [displayName] is the Autocomplete primary text (avoids DISPLAY_NAME / Pro).
     */
    suspend fun fetchPlaceDetails(
        placeId: String,
        displayName: String,
        session: AutocompleteSession?,
    ): PlacesResult<PlaceDetails>

    /**
     * Nearby Search Pro fields: id, displayName, location, formattedAddress, primaryType.
     */
    suspend fun searchNearby(
        includedType: String,
        center: LatLngPoint,
        radiusMeters: Double = DEFAULT_NEARBY_RADIUS_METERS,
        maxResultCount: Int = DEFAULT_NEARBY_MAX_RESULTS,
    ): PlacesResult<List<PlaceSummary>>

    companion object {
        const val DEFAULT_NEARBY_RADIUS_METERS = 5_000.0
        const val DEFAULT_NEARBY_MAX_RESULTS = 20
        const val AUTOCOMPLETE_MIN_CHARS = 3
        const val AUTOCOMPLETE_DEBOUNCE_MS = 300L
    }
}
