package com.example.gpstracker.feature.history.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.data.history.FakeRouteHistoryRepository
import com.example.gpstracker.core.data.history.NewRouteHistory
import com.example.gpstracker.feature.history.HistoryRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val history = FakeRouteHistoryRepository()
    private val settings = FakeSettingsRepository(distanceUnit = DistanceUnit.KM)
    private val zone = ZoneId.of("Asia/Karachi")
    private val clock = Clock.fixed(Instant.parse("2024-10-24T07:00:00Z"), zone)

    private fun TestScope.collectState(vm: HistoryViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

    private fun createVm() = HistoryViewModel(history, settings, clock)

    @Test
    fun `filters today by default`() = runTest {
        sample(startedAt = Instant.parse("2024-10-23T19:30:00Z").toEpochMilli())
        sample(startedAt = Instant.parse("2024-10-22T19:30:00Z").toEpochMilli(), dest = "Old")
        val vm = createVm()
        collectState(vm)
        advanceUntilIdle()
        assertEquals(HistoryRange.Today, vm.uiState.value.range)
        assertEquals(1, vm.uiState.value.groups.sumOf { it.items.size })
        assertEquals("Mosque", vm.uiState.value.groups.first().items.first().entry.destLabel)
    }

    @Test
    fun `all range shows every entry newest first`() = runTest {
        sample(startedAt = 1L, dest = "A")
        sample(startedAt = 2L, dest = "B")
        val vm = createVm()
        collectState(vm)
        vm.onEvent(HistoryEvent.RangeSelected(HistoryRange.All))
        advanceUntilIdle()
        val items = vm.uiState.value.groups.flatMap { it.items }
        assertEquals(listOf("B", "A"), items.map { it.entry.destLabel })
    }

    @Test
    fun `delete removes entry`() = runTest {
        val saved = sample(startedAt = Instant.parse("2024-10-23T20:00:00Z").toEpochMilli())
        val vm = createVm()
        collectState(vm)
        advanceUntilIdle()
        vm.onEvent(HistoryEvent.DeleteClicked(saved.id))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isEmpty)
        assertTrue(history.all().isEmpty())
    }

    private suspend fun sample(
        startedAt: Long,
        dest: String = "Mosque",
    ) = history.record(
        NewRouteHistory(
            originLabel = "Here",
            destLabel = dest,
            destPlaceId = "p",
            destLatitude = 33.7,
            destLongitude = 73.0,
            viaLabel = null,
            viaPlaceId = null,
            viaLatitude = null,
            viaLongitude = null,
            plannedDurationSeconds = 600,
            plannedDistanceMeters = 2000,
            encodedPolyline = "_p~iF~ps|U",
            startedAtMillis = startedAt,
        ),
    )
}
