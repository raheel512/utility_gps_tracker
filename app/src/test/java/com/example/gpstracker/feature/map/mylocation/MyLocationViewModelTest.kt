package com.example.gpstracker.feature.map.mylocation

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.format.AccuracyDisplay
import com.example.gpstracker.core.location.format.AccuracyUnit
import com.example.gpstracker.core.location.geocode.FakeReverseGeocoder
import com.example.gpstracker.core.location.geocode.GeocodedAddress
import com.example.gpstracker.core.location.userLocation
import com.example.gpstracker.core.map.AppMapType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyLocationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // encoding.csv: 20.3701125, 2.782234375 at 11 digits -> 7FG49QCJ+2VX
    private val fix = userLocation(latitude = 20.3701125, longitude = 2.782234375, accuracyMeters = 10f)
    private val address = GeocodedAddress("9th Ave", "F-9", "Islamabad", "Pakistan")

    private val locations = FakeLocationRepository()
    private val geocoder = FakeReverseGeocoder()
    private val settings = FakeSettingsRepository()

    private fun createViewModel() = MyLocationViewModel(locations, geocoder, settings)

    private fun TestScope.collectState(viewModel: MyLocationViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

    private fun TestScope.collectEffects(viewModel: MyLocationViewModel, into: MutableList<MyLocationEffect>) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { into.add(it) } }

    @Test
    fun `initial state has no location and cannot share or copy`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.location)
        assertNull(state.locationLine)
        assertFalse(state.canShareOrCopy)
        assertEquals(AppMapType.Satellite, state.mapType)
    }

    @Test
    fun `fix with address shows short code plus address`() = runTest {
        geocoder.result = address
        locations.currentLocation = fix
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("9QCJ+2VX, 9th Ave, F-9, Islamabad, Pakistan", state.locationLine)
        assertEquals("20.3701° N, 2.7822° E", state.coordinates)
        assertEquals(AccuracyDisplay(10, AccuracyUnit.Meters), state.accuracy)
        assertTrue(state.canShareOrCopy)
    }

    @Test
    fun `fix without address shows the full plus code`() = runTest {
        geocoder.result = null
        locations.currentLocation = fix
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        assertEquals("7FG49QCJ+2VX", viewModel.uiState.value.locationLine)
    }

    @Test
    fun `accuracy follows the distance unit setting`() = runTest {
        settings.setDistanceUnit(DistanceUnit.MI)
        locations.currentLocation = fix
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        assertEquals(AccuracyDisplay(33, AccuracyUnit.Feet), viewModel.uiState.value.accuracy)
    }

    @Test
    fun `location updates replace the current fix`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        locations.emit(userLocation(latitude = 47.0000625, longitude = 8.0000625))
        advanceUntilIdle()

        assertEquals("47.0001° N, 8.0001° E", viewModel.uiState.value.coordinates)
    }

    @Test
    fun `copy emits the card line`() = runTest {
        geocoder.result = address
        locations.currentLocation = fix
        val viewModel = createViewModel()
        collectState(viewModel)
        val effects = mutableListOf<MyLocationEffect>()
        collectEffects(viewModel, effects)
        advanceUntilIdle()

        viewModel.onEvent(MyLocationEvent.CopyClicked)
        advanceUntilIdle()

        assertEquals(
            listOf(MyLocationEffect.CopyToClipboard("9QCJ+2VX, 9th Ave, F-9, Islamabad, Pakistan")),
            effects,
        )
    }

    @Test
    fun `share emits line, coordinates and maps link`() = runTest {
        geocoder.result = address
        locations.currentLocation = fix
        val viewModel = createViewModel()
        collectState(viewModel)
        val effects = mutableListOf<MyLocationEffect>()
        collectEffects(viewModel, effects)
        advanceUntilIdle()

        viewModel.onEvent(MyLocationEvent.ShareClicked)
        advanceUntilIdle()

        val expected = listOf(
            "9QCJ+2VX, 9th Ave, F-9, Islamabad, Pakistan",
            "20.3701° N, 2.7822° E",
            "geo:20.370113,2.782234",
        ).joinToString("\n")
        assertEquals(listOf(MyLocationEffect.ShareText(expected)), effects)
    }

    @Test
    fun `copy and share do nothing before the first fix`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)
        val effects = mutableListOf<MyLocationEffect>()
        collectEffects(viewModel, effects)
        advanceUntilIdle()

        viewModel.onEvent(MyLocationEvent.CopyClicked)
        viewModel.onEvent(MyLocationEvent.ShareClicked)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
    }

    @Test
    fun `map layers toggles between satellite and normal`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)
        advanceUntilIdle()

        viewModel.onEvent(MyLocationEvent.ToggleMapType)
        advanceUntilIdle()
        assertEquals(AppMapType.Normal, viewModel.uiState.value.mapType)

        viewModel.onEvent(MyLocationEvent.ToggleMapType)
        advanceUntilIdle()
        assertEquals(AppMapType.Satellite, viewModel.uiState.value.mapType)
    }

    @Test
    fun `gps updates stop after the screen stops collecting`() = runTest {
        val viewModel = createViewModel()
        val collector = collectState(viewModel)
        advanceUntilIdle()
        assertEquals(1, locations.activeCollectors)

        collector.cancel()
        advanceTimeBy(MyLocationViewModel.STOP_TIMEOUT_MS * 2 + 1)
        advanceUntilIdle()
        assertEquals(0, locations.activeCollectors)
    }
}
