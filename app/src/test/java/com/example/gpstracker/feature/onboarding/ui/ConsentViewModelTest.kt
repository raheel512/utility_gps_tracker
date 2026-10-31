package com.example.gpstracker.feature.onboarding.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `continue disabled until terms accepted`() = runTest {
        val viewModel = ConsentViewModel()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.onEvent(ConsentEvent.ToggleTerms)
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `continue does not navigate when unchecked`() = runTest {
        val viewModel = ConsentViewModel()
        val effects = mutableListOf<ConsentEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(ConsentEvent.Continue)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        job.cancel()
    }

    @Test
    fun `continue navigates when checked`() = runTest {
        val viewModel = ConsentViewModel()
        val effects = mutableListOf<ConsentEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(ConsentEvent.ToggleTerms)
        viewModel.onEvent(ConsentEvent.Continue)
        advanceUntilIdle()

        assertTrue(effects.contains(ConsentEffect.NavigateNext))
        job.cancel()
    }
}
