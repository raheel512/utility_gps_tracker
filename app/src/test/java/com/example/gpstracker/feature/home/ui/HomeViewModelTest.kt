package com.example.gpstracker.feature.home.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.connectivity.FakeConnectivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(initial = true),
    ): HomeViewModel = HomeViewModel(connectivity)

    @Test
    fun `connectivity true maps to connected state`() = runTest {
        val viewModel = createViewModel(FakeConnectivityRepository(initial = true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.connected)
    }

    @Test
    fun `connectivity changes propagate to ui state`() = runTest {
        val connectivity = FakeConnectivityRepository(initial = true)
        val viewModel = createViewModel(connectivity)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.connected)

        connectivity.setConnected(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.connected)
    }

    @Test
    fun `each target emits its route effect or none`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<HomeNavEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        HomeTarget.entries.forEach { target ->
            effects.clear()
            viewModel.onEvent(HomeEvent.Select(target))
            advanceUntilIdle()

            val category = target.nearbyCategoryOrNull()
            val expectedRoute = target.toRouteOrNull()
            when {
                category != null -> assertEquals(
                    "Wrong maps effect for $target",
                    listOf(HomeNavEffect.OpenNearbyMaps(category)),
                    effects,
                )
                target == HomeTarget.FamousPlaces -> assertEquals(
                    "Famous places should open Google Maps",
                    listOf(HomeNavEffect.OpenFamousPlacesMaps),
                    effects,
                )
                expectedRoute == null -> assertTrue(
                    "Expected no effect for $target",
                    effects.isEmpty(),
                )
                else -> assertEquals(
                    "Wrong effect for $target",
                    listOf(HomeNavEffect.Navigate(expectedRoute)),
                    effects,
                )
            }
        }

        job.cancel()
    }

    @Test
    fun `voice navigation and country code open their screens`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<HomeNavEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(HomeEvent.Select(HomeTarget.VoiceNavigation))
        viewModel.onEvent(HomeEvent.Select(HomeTarget.CountryCode))
        advanceUntilIdle()

        assertEquals(
            com.example.gpstracker.core.navigation.Route.Voice,
            (effects[0] as HomeNavEffect.Navigate).route,
        )
        assertEquals(
            com.example.gpstracker.core.navigation.Route.CountryCode,
            (effects[1] as HomeNavEffect.Navigate).route,
        )
        job.cancel()
    }
}
