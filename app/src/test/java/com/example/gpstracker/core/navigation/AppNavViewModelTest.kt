package com.example.gpstracker.core.navigation

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.common.TestDispatcherProvider
import com.example.gpstracker.core.data.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `start destination is Consent when onboarding incomplete`() = runTest {
        val viewModel = AppNavViewModel(
            settingsRepository = FakeSettingsRepository(onboardingCompleted = false),
            dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
        )
        advanceUntilIdle()

        assertEquals(StartDestination.Consent, viewModel.startDestination.value)
        assertTrue(viewModel.isSettingsReady)
    }

    @Test
    fun `start destination is Home when onboarding completed`() = runTest {
        val viewModel = AppNavViewModel(
            settingsRepository = FakeSettingsRepository(onboardingCompleted = true),
            dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
        )
        advanceUntilIdle()

        assertEquals(StartDestination.Home, viewModel.startDestination.value)
        assertTrue(viewModel.isSettingsReady)
    }
}
