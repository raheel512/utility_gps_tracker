package com.example.gpstracker.feature.paywall.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.common.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(): PaywallViewModel = PaywallViewModel(
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    @Test
    fun `close emits navigate up effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<PaywallEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(PaywallEvent.Close)
        advanceUntilIdle()

        assertEquals(listOf(PaywallEffect.NavigateUp), effects)
        job.cancel()
    }

    @Test
    fun `purchase clicked is a no-op for now`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<PaywallEffect>()
        val before = viewModel.uiState.value
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(PaywallEvent.PurchaseClicked)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertEquals(before, viewModel.uiState.value)
        assertFalse(viewModel.uiState.value.isPurchasing)
        job.cancel()
    }

    @Test
    fun `restore purchases is a no-op for now`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<PaywallEffect>()
        val before = viewModel.uiState.value
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(PaywallEvent.RestorePurchases)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertEquals(before, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun `price and trial text are hidden when null`() = runTest {
        val state = createViewModel().uiState.value

        assertNull(state.priceText)
        assertNull(state.trialText)
    }

    @Test
    fun `terms and privacy emit open url effects`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<PaywallEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(PaywallEvent.OpenTerms)
        viewModel.onEvent(PaywallEvent.OpenPrivacy)
        advanceUntilIdle()

        assertEquals(
            listOf(PaywallEffect.OpenTermsUrl, PaywallEffect.OpenPrivacyUrl),
            effects,
        )
        job.cancel()
    }
}
