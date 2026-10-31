package com.example.gpstracker.feature.voice.ui

import com.example.gpstracker.R
import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.data.connectivity.FakeConnectivityRepository
import com.example.gpstracker.core.data.history.FakeRouteHistoryRepository
import com.example.gpstracker.core.data.saved.FakeSavedPlacesRepository
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.geocode.FakeReverseGeocoder
import com.example.gpstracker.core.location.userLocation
import com.example.gpstracker.core.permissions.MicrophonePermissionState
import com.example.gpstracker.core.places.DefaultAutocompleteSessionManager
import com.example.gpstracker.core.places.FakePlacesRepository
import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.routes.FakeRoutesRepository
import com.example.gpstracker.core.routes.RoutesResult
import com.example.gpstracker.core.speech.FakeSpeechRecognizerClient
import com.example.gpstracker.core.speech.SpeechState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val speech = FakeSpeechRecognizerClient()
    private val places = FakePlacesRepository()
    private val sessions = DefaultAutocompleteSessionManager(places)
    private val routes = FakeRoutesRepository()
    private val history = FakeRouteHistoryRepository()
    private val locations = FakeLocationRepository(currentLocation = userLocation(33.7, 73.0))
    private val settings = FakeSettingsRepository()
    private val saved = FakeSavedPlacesRepository()
    private val connectivity = FakeConnectivityRepository(true)
    private val geocoder = FakeReverseGeocoder()
    private val clock = Clock.fixed(Instant.parse("2024-10-24T07:00:00Z"), ZoneOffset.UTC)

    private fun createVm() = VoiceViewModel(
        speechClient = speech,
        placesRepository = places,
        sessionManager = sessions,
        routesRepository = routes,
        historyRepository = history,
        locationRepository = locations,
        settingsRepository = settings,
        savedPlacesRepository = saved,
        connectivityRepository = connectivity,
        reverseGeocoder = geocoder,
        clock = clock,
    )

    @Test
    fun `uses the next transcript when the first has no suggestions`() = runTest {
        places.autocompleteQueue = ArrayDeque(
            listOf(
                PlacesResult.Ok(emptyList()),
                PlacesResult.Ok(listOf(PlaceSuggestion("pid", "Faisal Mosque", "Shah Faisal Ave"))),
            ),
        )
        places.detailsResult = PlacesResult.Ok(
            PlaceDetails("pid", "Faisal Mosque", "Shah Faisal Ave, E-8, Islamabad", 33.73, 73.04),
        )
        val vm = createVm()
        vm.onEvent(VoiceEvent.ScreenOpened(MicrophonePermissionState.Granted))
        advanceUntilIdle()
        speech.emit(
            SpeechState.Result(
                listOf("zzzzzz", "navigate to faisal mosque"),
            ),
        )
        advanceUntilIdle()
        assertEquals(VoicePhase.Results, vm.uiState.value.phase)
        assertEquals("Faisal Mosque", vm.uiState.value.destination?.name)
        assertEquals(0, places.nearbyCalls)
        assertTrue(places.detailsCalls >= 1)
    }

    @Test
    fun `records history without stats when the route call fails`() = runTest {
        places.autocompleteResult = PlacesResult.Ok(
            listOf(PlaceSuggestion("pid", "Faisal Mosque", "Ave")),
        )
        places.detailsResult = PlacesResult.Ok(
            PlaceDetails("pid", "Faisal Mosque", "Shah Faisal Ave", 33.73, 73.04),
        )
        routes.result = RoutesResult.HttpError(500)
        val vm = createVm()
        vm.onEvent(VoiceEvent.ScreenOpened(MicrophonePermissionState.Granted))
        advanceUntilIdle()
        speech.emit(SpeechState.Result(listOf("navigate to faisal mosque")))
        advanceUntilIdle()
        assertEquals(1, routes.calls)
        assertEquals(null, vm.uiState.value.plan)
        val effects = mutableListOf<VoiceEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(VoiceEvent.StartNavigationClicked)
        advanceUntilIdle()
        assertEquals(1, routes.calls)
        assertTrue(effects.any { it is VoiceEffect.OpenMaps })
        assertTrue(history.all().isEmpty())
        vm.onEvent(VoiceEvent.StartLaunched)
        advanceUntilIdle()
        assertEquals(1, history.all().size)
        assertFalse(history.all().first().hasRouteStats)
        assertEquals("Faisal Mosque", history.all().first().destLabel)
    }

    @Test
    fun `unavailable recognizer shows a message and does not start`() = runTest {
        speech.available = false
        val vm = createVm()
        vm.onEvent(VoiceEvent.ScreenOpened(MicrophonePermissionState.Granted))
        advanceUntilIdle()
        assertEquals(0, speech.startCount)
        assertEquals(R.string.voice_unavailable, vm.uiState.value.snackbarMessageRes)
        assertEquals(VoicePhase.Idle, vm.uiState.value.phase)
    }
}
