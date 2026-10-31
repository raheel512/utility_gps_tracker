package com.example.gpstracker.core.places

import kotlinx.coroutines.delay

class FakePlacesRepository : PlacesRepository {
    var autocompleteDelayMs: Long = 0
    var autocompleteResult: PlacesResult<List<PlaceSuggestion>> =
        PlacesResult.Ok(emptyList())
    var detailsResult: PlacesResult<PlaceDetails> = PlacesResult.Failure(PlacesFailureReason.Unknown)
    var nearbyResult: PlacesResult<List<PlaceSummary>> = PlacesResult.Ok(emptyList())

    var autocompleteQueue: ArrayDeque<PlacesResult<List<PlaceSuggestion>>>? = null

    var autocompleteCalls = 0
    var detailsCalls = 0
    var nearbyCalls = 0
    var lastAutocompleteQuery: String? = null
    var sessionsCreated = 0

    private val sessions = mutableListOf<FakeSession>()

    override fun newAutocompleteSession(): AutocompleteSession {
        sessionsCreated++
        return FakeSession(sessionsCreated).also { sessions.add(it) }
    }

    override suspend fun findAutocompletePredictions(
        query: String,
        session: AutocompleteSession,
        locationBias: LatLngPoint?,
    ): PlacesResult<List<PlaceSuggestion>> {
        autocompleteCalls++
        lastAutocompleteQuery = query
        if (autocompleteDelayMs > 0) delay(autocompleteDelayMs)
        autocompleteQueue?.removeFirstOrNull()?.let { return it }
        return autocompleteResult
    }

    override suspend fun fetchPlaceDetails(
        placeId: String,
        displayName: String,
        session: AutocompleteSession?,
    ): PlacesResult<PlaceDetails> {
        detailsCalls++
        return detailsResult
    }

    override suspend fun searchNearby(
        includedType: String,
        center: LatLngPoint,
        radiusMeters: Double,
        maxResultCount: Int,
    ): PlacesResult<List<PlaceSummary>> {
        nearbyCalls++
        return nearbyResult
    }

    class FakeSession(val id: Int) : AutocompleteSession
}
