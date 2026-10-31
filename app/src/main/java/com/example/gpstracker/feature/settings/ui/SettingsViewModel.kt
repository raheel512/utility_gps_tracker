package com.example.gpstracker.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val pickerState = MutableStateFlow(PickerState())
    private val rateDialogVisible = MutableStateFlow(false)

    private data class PickerState(
        val visible: Boolean = false,
        val draft: ThemeMode = ThemeMode.Auto,
    )

    private val settingsFlow = combine(
        settingsRepository.themeMode,
        settingsRepository.selectedLanguageCode,
        settingsRepository.roadAlertsEnabled,
        settingsRepository.voiceGuidanceEnabled,
        settingsRepository.distanceUnit,
    ) { theme, language, roadAlerts, voice, unit ->
        SettingsSnapshot(
            themeMode = theme,
            languageCode = language,
            roadAlertsEnabled = roadAlerts,
            voiceGuidanceEnabled = voice,
            distanceUnit = unit,
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsFlow,
        pickerState,
        rateDialogVisible,
    ) { snapshot, picker, rateVisible ->
        SettingsUiState(
            themeMode = snapshot.themeMode,
            languageCode = snapshot.languageCode,
            languageDisplayName = snapshot.languageCode, // resolved in UI via string arrays
            roadAlertsEnabled = snapshot.roadAlertsEnabled,
            voiceGuidanceEnabled = snapshot.voiceGuidanceEnabled,
            distanceUnit = snapshot.distanceUnit,
            themePickerVisible = picker.visible,
            draftThemeMode = picker.draft,
            rateDialogVisible = rateVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    private val effectBus = EventBus<SettingsEffect>()
    val effects = effectBus.events

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.HelpClick -> {
                // TODO(route): help destination has no screenshot
            }
            SettingsEvent.ThemeClick -> {
                pickerState.update {
                    PickerState(visible = true, draft = uiState.value.themeMode)
                }
            }
            SettingsEvent.ThemePickerDismiss -> {
                pickerState.update { it.copy(visible = false) }
            }
            is SettingsEvent.ThemeDraftSelect -> {
                pickerState.update { it.copy(draft = event.mode) }
            }
            SettingsEvent.ThemePickerSave -> {
                val draft = pickerState.value.draft
                viewModelScope.launch {
                    withContext(dispatchers.io) {
                        settingsRepository.setThemeMode(draft)
                    }
                    pickerState.update { it.copy(visible = false) }
                }
            }
            SettingsEvent.LanguageClick -> {
                viewModelScope.launch { effectBus.emit(SettingsEffect.NavigateToLanguage) }
            }
            SettingsEvent.NavigationRoutingClick -> {
                // TODO(route): Navigation Routing screen has no screenshot
            }
            is SettingsEvent.RoadAlertsToggle -> {
                viewModelScope.launch {
                    withContext(dispatchers.io) {
                        settingsRepository.setRoadAlertsEnabled(event.enabled)
                    }
                }
            }
            is SettingsEvent.VoiceGuidanceToggle -> {
                viewModelScope.launch {
                    withContext(dispatchers.io) {
                        settingsRepository.setVoiceGuidanceEnabled(event.enabled)
                    }
                }
            }
            is SettingsEvent.DistanceUnitSelect -> {
                viewModelScope.launch {
                    withContext(dispatchers.io) {
                        settingsRepository.setDistanceUnit(event.unit)
                    }
                }
            }
            SettingsEvent.PrivacyPolicyClick -> {
                viewModelScope.launch { effectBus.emit(SettingsEffect.OpenPrivacyPolicy) }
            }
            SettingsEvent.RateClick -> {
                rateDialogVisible.value = true
            }
            SettingsEvent.RateDialogDismiss -> {
                rateDialogVisible.value = false
            }
            is SettingsEvent.RateStarSelected -> {
                rateDialogVisible.value = false
                viewModelScope.launch { effectBus.emit(SettingsEffect.OpenPlayStore) }
            }
            SettingsEvent.InviteClick -> {
                viewModelScope.launch { effectBus.emit(SettingsEffect.ShareInvite) }
            }
        }
    }

    private data class SettingsSnapshot(
        val themeMode: ThemeMode,
        val languageCode: String,
        val roadAlertsEnabled: Boolean,
        val voiceGuidanceEnabled: Boolean,
        val distanceUnit: DistanceUnit,
    )
}
