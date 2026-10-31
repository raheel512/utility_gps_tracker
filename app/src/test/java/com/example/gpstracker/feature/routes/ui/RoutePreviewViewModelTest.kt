package com.example.gpstracker.feature.routes.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.data.connectivity.FakeConnectivityRepository
import com.example.gpstracker.core.data.history.FakeRouteHistoryRepository
import com.example.gpstracker.core.data.saved.FakeSavedPlacesRepository
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.geocode.FakeReverseGeocoder
import com.example.gpstracker.core.location.userLocation
import com.example.gpstracker.core.navigation.Route
import com.example.gpstracker.core.places.DefaultAutocompleteSessionManager
import com.example.gpstracker.core.places.FakePlacesRepository
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.routes.FakeRoutesRepository
import com.example.gpstracker.core.routes.RoutePlan
import com.example.gpstracker.core.routes.RoutesResult
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
class RoutePreviewViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val routes = FakeRoutesRepository()
    private val history = FakeRouteHistoryRepository()
    private val locations = FakeLocationRepository(
        currentLocation = userLocation(33.7, 73.0),
    )
    private val settings = FakeSettingsRepository()
    private val saved = FakeSavedPlacesRepository()
    private val connectivity = FakeConnectivityRepository(true)
    private val places = FakePlacesRepository()
    private val sessions = DefaultAutocompleteSessionManager(places)
    private val geocoder = FakeReverseGeocoder()
    private val clock = Clock.fixed(Instant.parse("2024-10-24T07:00:00Z"), ZoneOffset.UTC)

    private val plan = RoutePlan(
        encodedPolyline = "_p~iF~ps|U",
        points = listOf(LatLngPoint(33.7, 73.0), LatLngPoint(33.73, 73.04)),
        distanceMeters = 6400,
        durationSeconds = 840,
        viaLabel = null,
    )

    private fun createVm() = RoutePreviewViewModel(
        args = Route.RoutePreview(
            destName = "Faisal Mosque",
            destLatitude = 33.73,
            destLongitude = 73.04,
            destAddress = "E-8",
            destPlaceId = "pid",
        ),
        routesRepository = routes,
        historyRepository = history,
        locationRepository = locations,
        settingsRepository = settings,
        savedPlacesRepository = saved,
        connectivityRepository = connectivity,
        placesRepository = places,
        sessionManager = sessions,
        reverseGeocoder = geocoder,
        clock = clock,
    )

    @Test
    fun `success computes one route`() = runTest {
        routes.result = RoutesResult.Ok(plan)
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(1, routes.calls)
        assertEquals(plan, vm.uiState.value.plan)
        assertTrue(vm.uiState.value.canStart)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `empty route sets error`() = runTest {
        routes.result = RoutesResult.Empty
        val vm = createVm()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canStart)
        assertTrue(vm.uiState.value.errorMessageRes != null)
    }

    @Test
    fun `offline shows no internet`() = runTest {
        connectivity.setConnected(false)
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(0, routes.calls)
        assertTrue(vm.uiState.value.showNoInternetDialog)
    }

    @Test
    fun `http error sets request failed`() = runTest {
        routes.result = RoutesResult.HttpError(500)
        val vm = createVm()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canStart)
        assertTrue(vm.uiState.value.errorMessageRes != null)
    }

    @Test
    fun `records history only after successful launch`() = runTest {
        routes.result = RoutesResult.Ok(plan)
        val vm = createVm()
        advanceUntilIdle()
        val effects = mutableListOf<RoutePreviewEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(RoutePreviewEvent.StartNavigationClicked)
        advanceUntilIdle()
        assertTrue(effects.any { it is RoutePreviewEffect.OpenMaps })
        assertEquals(0, history.all().size)
        vm.onEvent(RoutePreviewEvent.StartLaunched)
        advanceUntilIdle()
        assertEquals(1, history.all().size)
        assertEquals("Faisal Mosque", history.all().first().destLabel)
    }

    @Test
    fun `via from nav args is included on first compute`() = runTest {
        routes.result = RoutesResult.Ok(plan)
        val vm = RoutePreviewViewModel(
            args = Route.RoutePreview(
                destName = "Faisal Mosque",
                destLatitude = 33.73,
                destLongitude = 73.04,
                destAddress = "E-8",
                destPlaceId = "pid",
                viaName = "Blue Area",
                viaPlaceId = "via",
                viaLatitude = 33.71,
                viaLongitude = 73.05,
            ),
            routesRepository = routes,
            historyRepository = history,
            locationRepository = locations,
            settingsRepository = settings,
            savedPlacesRepository = saved,
            connectivityRepository = connectivity,
            placesRepository = places,
            sessionManager = sessions,
            reverseGeocoder = geocoder,
            clock = clock,
        )
        advanceUntilIdle()
        assertEquals("via", routes.lastRequest?.viaPlaceId)
        assertEquals(33.71, routes.lastRequest?.viaLatitude)
        assertEquals(73.05, routes.lastRequest?.viaLongitude)
        assertEquals("Blue Area", vm.uiState.value.viaLabel)
    }

    @Test
    fun `failed launch does not record`() = runTest {
        routes.result = RoutesResult.Ok(plan)
        val vm = createVm()
        advanceUntilIdle()
        vm.onEvent(RoutePreviewEvent.StartFailed)
        advanceUntilIdle()
        assertEquals(0, history.all().size)
    }
}
