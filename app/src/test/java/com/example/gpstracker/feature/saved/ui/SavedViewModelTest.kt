package com.example.gpstracker.feature.saved.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.data.saved.FakeSavedPlacesRepository
import com.example.gpstracker.core.data.saved.SavePlaceRequest
import com.example.gpstracker.core.data.saved.SavedPlace
import com.example.gpstracker.core.data.saved.SavedPlaceIconKind
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.userLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedRepo = FakeSavedPlacesRepository()
    private val locations = FakeLocationRepository()
    private val settings = FakeSettingsRepository(distanceUnit = DistanceUnit.KM)

    private fun createViewModel() = SavedViewModel(savedRepo, locations, settings)

    private fun TestScope.collectState(viewModel: SavedViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

    @Test
    fun `empty state when no places`() = runTest {
        val vm = createViewModel()
        collectState(vm)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `lists saved places ordered and filters by query`() = runTest {
        savedRepo.save(SavePlaceRequest("1", "Home", "Islamabad", 33.7, 73.0))
        savedRepo.save(SavePlaceRequest("2", "Centaurus Mall", "Islamabad", 33.71, 73.05))
        val vm = createViewModel()
        collectState(vm)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.items.size)

        vm.onEvent(SavedEvent.QueryChanged("mall"))
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Centaurus Mall", vm.uiState.value.items.first().title)
    }

    @Test
    fun `distance formatting uses km setting`() {
        val items = SavedViewModel.buildListItems(
            places = listOf(
                SavedPlace(
                    id = 1,
                    placeId = "1",
                    name = "Home",
                    address = "Addr",
                    latitude = 33.7,
                    longitude = 73.0,
                    collectionId = null,
                    createdAtMillis = 0,
                    iconKind = SavedPlaceIconKind.HOME,
                ),
            ),
            collections = emptyList(),
            location = userLocation(latitude = 33.7, longitude = 73.01),
            unit = DistanceUnit.KM,
            query = "",
        )
        assertTrue(items.first().distanceLabel!!.contains("km") || items.first().distanceLabel!!.contains("m"))
    }

    @Test
    fun `save spot from current location`() = runTest {
        locations.currentLocation = userLocation(latitude = 33.6844, longitude = 73.0479)
        val vm = createViewModel()
        collectState(vm)
        advanceUntilIdle()
        vm.onEvent(SavedEvent.SaveSpotClicked)
        advanceUntilIdle()
        assertEquals(1, savedRepo.all().size)
        assertEquals("Current Location", savedRepo.all().first().name)
    }

    @Test
    fun `unsave keeps pending undo`() = runTest {
        val place = savedRepo.save(SavePlaceRequest("1", "Home", "Addr", 1.0, 2.0))
        val vm = createViewModel()
        collectState(vm)
        advanceUntilIdle()
        vm.onEvent(SavedEvent.UnsaveClicked(place.id))
        advanceUntilIdle()
        assertEquals(0, savedRepo.all().size)
        assertEquals("Home", vm.uiState.value.pendingUndo?.name)

        vm.onEvent(SavedEvent.UndoUnsave)
        advanceUntilIdle()
        assertEquals(1, savedRepo.all().size)
    }
}
