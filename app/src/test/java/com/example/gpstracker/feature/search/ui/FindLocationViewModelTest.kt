package com.example.gpstracker.feature.search.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.connectivity.FakeConnectivityRepository
import com.example.gpstracker.core.data.saved.FakeSavedPlacesRepository
import com.example.gpstracker.R
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.geocode.FakeReverseGeocoder
import com.example.gpstracker.core.location.geocode.GeocodedAddress
import com.example.gpstracker.core.location.userLocation
import com.example.gpstracker.core.places.DefaultAutocompleteSessionManager
import com.example.gpstracker.core.places.FakePlacesRepository
import com.example.gpstracker.core.places.FakeRecentSearchRepository
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FindLocationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val places = FakePlacesRepository()
    private val recent = FakeRecentSearchRepository()
    private val saved = FakeSavedPlacesRepository()
    private val locations = FakeLocationRepository()
    private val geocoder = FakeReverseGeocoder()
    private val connectivity = FakeConnectivityRepository(initial = true)
    private val sessions = DefaultAutocompleteSessionManager(places)

    private fun createViewModel() = FindLocationViewModel(
        placesRepository = places,
        recentSearchRepository = recent,
        savedPlacesRepository = saved,
        locationRepository = locations,
        reverseGeocoder = geocoder,
        connectivityRepository = connectivity,
        sessionManager = sessions,
    )

    @Test
    fun `short query does not call autocomplete`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onEvent(FindLocationEvent.DestinationQueryChanged("ab"))
        advanceTimeBy(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS + 50)
        advanceUntilIdle()
        assertEquals(0, places.autocompleteCalls)
    }

    @Test
    fun `debounces autocomplete until 300ms after last change`() = runTest(testDispatcher) {
        places.autocompleteResult = PlacesResult.Ok(
            listOf(PlaceSuggestion("1", "Cafe", "Street")),
        )
        val vm = createViewModel()
        vm.onEvent(FindLocationEvent.DestinationQueryChanged("caf"))
        advanceTimeBy(100)
        vm.onEvent(FindLocationEvent.DestinationQueryChanged("cafe"))
        advanceTimeBy(100)
        assertEquals(0, places.autocompleteCalls)
        advanceTimeBy(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS)
        advanceUntilIdle()
        assertEquals(1, places.autocompleteCalls)
        assertEquals("cafe", places.lastAutocompleteQuery)
        assertEquals(1, vm.uiState.value.suggestions.size)
    }

    @Test
    fun `cancels stale autocomplete when query changes`() = runTest(testDispatcher) {
        places.autocompleteDelayMs = 500
        places.autocompleteResult = PlacesResult.Ok(
            listOf(PlaceSuggestion("old", "Old", "")),
        )
        val vm = createViewModel()
        vm.onEvent(FindLocationEvent.DestinationQueryChanged("oldquery"))
        advanceTimeBy(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS)
        advanceUntilIdle()
        // First request in flight…
        places.autocompleteResult = PlacesResult.Ok(
            listOf(PlaceSuggestion("new", "New", "")),
        )
        places.autocompleteDelayMs = 0
        vm.onEvent(FindLocationEvent.DestinationQueryChanged("newquery"))
        advanceTimeBy(PlacesRepository.AUTOCOMPLETE_DEBOUNCE_MS)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.suggestions.any { it.placeId == "new" })
        assertTrue(vm.uiState.value.suggestions.none { it.placeId == "old" })
    }

    @Test
    fun `selecting suggestion resets session`() = runTest(testDispatcher) {
        places.detailsResult = PlacesResult.Ok(
            com.example.gpstracker.core.places.PlaceDetails(
                placeId = "1",
                name = "Cafe",
                formattedAddress = "Street",
                latitude = 1.0,
                longitude = 2.0,
            ),
        )
        val vm = createViewModel()
        val genBefore = sessions.generation()
        vm.onEvent(
            FindLocationEvent.SuggestionSelected(
                PlaceSuggestion("1", "Cafe", "Street"),
            ),
        )
        advanceUntilIdle()
        assertEquals(genBefore + 1, sessions.generation())
        assertEquals("Cafe", vm.uiState.value.selectedPlace?.name)
        assertEquals(1, recent.all().size)
    }

    @Test
    fun `screen stopped resets session`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val genBefore = sessions.generation()
        vm.onEvent(FindLocationEvent.ScreenStopped)
        assertEquals(genBefore + 1, sessions.generation())
    }

    @Test
    fun `save toggles selected place into saved repository`() = runTest(testDispatcher) {
        places.detailsResult = PlacesResult.Ok(
            com.example.gpstracker.core.places.PlaceDetails(
                placeId = "1",
                name = "Cafe",
                formattedAddress = "Street",
                latitude = 1.0,
                longitude = 2.0,
            ),
        )
        val vm = createViewModel()
        vm.onEvent(
            FindLocationEvent.SuggestionSelected(PlaceSuggestion("1", "Cafe", "Street")),
        )
        advanceUntilIdle()
        vm.onEvent(FindLocationEvent.SaveClicked)
        advanceUntilIdle()
        assertEquals(1, saved.all().size)
        assertTrue(vm.uiState.value.isSelectedSaved)
    }

    @Test
    fun `navigate emits route preview for selected place`() = runTest(testDispatcher) {
        places.detailsResult = PlacesResult.Ok(
            com.example.gpstracker.core.places.PlaceDetails(
                placeId = "1",
                name = "Cafe",
                formattedAddress = "Street",
                latitude = 1.0,
                longitude = 2.0,
            ),
        )
        val vm = createViewModel()
        vm.onEvent(FindLocationEvent.SuggestionSelected(PlaceSuggestion("1", "Cafe", "Street")))
        advanceUntilIdle()
        val effects = mutableListOf<FindLocationEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(FindLocationEvent.NavigateClicked)
        advanceUntilIdle()
        val preview = effects.filterIsInstance<FindLocationEffect.OpenRoutePreview>().single()
        assertEquals("Cafe", preview.destName)
        assertEquals("1", preview.destPlaceId)
    }

    @Test
    fun `opening the screen stores the gps fix and label`() = runTest(testDispatcher) {
        locations.currentLocation = userLocation(33.6844, 73.0479)
        geocoder.result = GeocodedAddress(locality = "Islamabad")
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(33.6844, state.userLatitude!!, 0.0001)
        assertEquals(73.0479, state.userLongitude!!, 0.0001)
        assertEquals(1, state.locationRevision)
        assertTrue(state.myLocationLine!!.contains("Islamabad"))
        assertEquals(false, state.locating)
    }

    @Test
    fun `gps button replaces my location with a new fix`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(null, vm.uiState.value.userLatitude)
        locations.currentLocation = userLocation(31.5204, 74.3587)
        geocoder.result = GeocodedAddress(locality = "Lahore")
        vm.onEvent(FindLocationEvent.UseGpsClicked)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(31.5204, state.userLatitude!!, 0.0001)
        assertEquals(74.3587, state.userLongitude!!, 0.0001)
        assertTrue(state.myLocationLine!!.contains("Lahore"))
        assertEquals(false, state.locating)
    }

    @Test
    fun `gps button reports failure when there is no fix`() = runTest(testDispatcher) {
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(null, vm.uiState.value.errorMessageRes)
        vm.onEvent(FindLocationEvent.UseGpsClicked)
        advanceUntilIdle()
        assertEquals(R.string.find_location_gps_unavailable, vm.uiState.value.errorMessageRes)
        assertEquals(null, vm.uiState.value.userLatitude)
        assertEquals(false, vm.uiState.value.locating)
    }
}
