package com.example.gpstracker.feature.onboarding.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.common.TestDispatcherProvider
import com.example.gpstracker.core.data.FakeSettingsRepository
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
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        settings: FakeSettingsRepository = FakeSettingsRepository(),
    ): OnboardingViewModel = OnboardingViewModel(
        settingsRepository = settings,
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    @Test
    fun `next advances page until last`() = runTest {
        val viewModel = createViewModel()
        val lastIndex = viewModel.uiState.value.slides.lastIndex
        assertEquals(0, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.isLastPage)

        repeat(lastIndex) {
            viewModel.onEvent(OnboardingEvent.Next)
            advanceUntilIdle()
        }

        assertEquals(lastIndex, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.isLastPage)
    }

    @Test
    fun `next on last page completes onboarding`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        val effects = mutableListOf<OnboardingEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(OnboardingEvent.PageChanged(viewModel.uiState.value.slides.lastIndex))
        viewModel.onEvent(OnboardingEvent.Next)
        advanceUntilIdle()

        assertTrue(settings.currentOnboardingCompleted())
        assertTrue(effects.contains(OnboardingEffect.Finished))
        job.cancel()
    }

    @Test
    fun `skip completes onboarding from first page`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        val effects = mutableListOf<OnboardingEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(OnboardingEvent.Skip)
        advanceUntilIdle()

        assertTrue(settings.currentOnboardingCompleted())
        assertTrue(effects.contains(OnboardingEffect.Finished))
        assertFalse(viewModel.uiState.value.isCompleting)
        job.cancel()
    }
}
