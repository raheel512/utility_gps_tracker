package com.example.gpstracker.feature.settings.ui

import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.Auto,
    val languageCode: String = "en",
    val languageDisplayName: String = "English",
    val roadAlertsEnabled: Boolean = true,
    val voiceGuidanceEnabled: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val themePickerVisible: Boolean = false,
    val draftThemeMode: ThemeMode = ThemeMode.Auto,
    val rateDialogVisible: Boolean = false,
)

sealed interface SettingsEvent {
    data object HelpClick : SettingsEvent
    data object ThemeClick : SettingsEvent
    data object ThemePickerDismiss : SettingsEvent
    data class ThemeDraftSelect(val mode: ThemeMode) : SettingsEvent
    data object ThemePickerSave : SettingsEvent
    data object LanguageClick : SettingsEvent
    data object NavigationRoutingClick : SettingsEvent
    data class RoadAlertsToggle(val enabled: Boolean) : SettingsEvent
    data class VoiceGuidanceToggle(val enabled: Boolean) : SettingsEvent
    data class DistanceUnitSelect(val unit: DistanceUnit) : SettingsEvent
    data object PrivacyPolicyClick : SettingsEvent
    data object RateClick : SettingsEvent
    data object RateDialogDismiss : SettingsEvent
    data class RateStarSelected(val stars: Int) : SettingsEvent
    data object InviteClick : SettingsEvent
}

sealed interface SettingsEffect {
    data object NavigateToLanguage : SettingsEffect
    data object OpenPrivacyPolicy : SettingsEffect
    data object OpenPlayStore : SettingsEffect
    data object ShareInvite : SettingsEffect
}
