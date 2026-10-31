package com.example.gpstracker.feature.settings.ui

import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.common.TestDispatcherProvider
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.data.ThemeMode
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
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        settings: FakeSettingsRepository = FakeSettingsRepository(),
    ): SettingsViewModel = SettingsViewModel(
        settingsRepository = settings,
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    @Test
    fun `defaults match repository defaults`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.Auto, state.themeMode)
        assertTrue(state.roadAlertsEnabled)
        assertTrue(state.voiceGuidanceEnabled)
        assertEquals(DistanceUnit.KM, state.distanceUnit)
        assertFalse(state.themePickerVisible)
    }

    @Test
    fun `road alerts toggle persists`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.RoadAlertsToggle(false))
        advanceUntilIdle()

        assertFalse(settings.currentRoadAlertsEnabled())
        assertFalse(viewModel.uiState.value.roadAlertsEnabled)
    }

    @Test
    fun `voice guidance toggle persists`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.VoiceGuidanceToggle(false))
        advanceUntilIdle()

        assertFalse(settings.currentVoiceGuidanceEnabled())
        assertFalse(viewModel.uiState.value.voiceGuidanceEnabled)
    }

    @Test
    fun `distance unit selection persists`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.DistanceUnitSelect(DistanceUnit.MI))
        advanceUntilIdle()

        assertEquals(DistanceUnit.MI, settings.currentDistanceUnit())
        assertEquals(DistanceUnit.MI, viewModel.uiState.value.distanceUnit)
    }

    @Test
    fun `theme picker save updates theme flow`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = createViewModel(settings)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ThemeClick)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.themePickerVisible)

        viewModel.onEvent(SettingsEvent.ThemeDraftSelect(ThemeMode.Dark))
        viewModel.onEvent(SettingsEvent.ThemePickerSave)
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, settings.currentThemeMode())
        assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
        assertFalse(viewModel.uiState.value.themePickerVisible)
    }

    @Test
    fun `theme picker dismiss does not save draft`() = runTest {
        val settings = FakeSettingsRepository(themeMode = ThemeMode.Auto)
        val viewModel = createViewModel(settings)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ThemeClick)
        viewModel.onEvent(SettingsEvent.ThemeDraftSelect(ThemeMode.Light))
        viewModel.onEvent(SettingsEvent.ThemePickerDismiss)
        advanceUntilIdle()

        assertEquals(ThemeMode.Auto, settings.currentThemeMode())
        assertFalse(viewModel.uiState.value.themePickerVisible)
    }

    @Test
    fun `language click emits navigate effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(SettingsEvent.LanguageClick)
        advanceUntilIdle()

        assertEquals(listOf(SettingsEffect.NavigateToLanguage), effects)
        job.cancel()
    }

    @Test
    fun `privacy rate and invite emit link effects`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(SettingsEvent.PrivacyPolicyClick)
        viewModel.onEvent(SettingsEvent.RateClick)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.rateDialogVisible)

        viewModel.onEvent(SettingsEvent.RateStarSelected(5))
        viewModel.onEvent(SettingsEvent.InviteClick)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.rateDialogVisible)
        assertEquals(
            listOf(
                SettingsEffect.OpenPrivacyPolicy,
                SettingsEffect.OpenPlayStore,
                SettingsEffect.ShareInvite,
            ),
            effects,
        )
        job.cancel()
    }

    @Test
    fun `rate dialog dismiss does not open play store`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(SettingsEvent.RateClick)
        viewModel.onEvent(SettingsEvent.RateDialogDismiss)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.rateDialogVisible)
        assertTrue(effects.isEmpty())
        job.cancel()
    }

    @Test
    fun `routing and help clicks emit no effects`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(SettingsEvent.HelpClick)
        viewModel.onEvent(SettingsEvent.NavigationRoutingClick)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        job.cancel()
    }
}
