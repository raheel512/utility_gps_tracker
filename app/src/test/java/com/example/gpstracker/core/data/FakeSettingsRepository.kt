package com.example.gpstracker.core.data

import com.example.gpstracker.core.common.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    onboardingCompleted: Boolean = false,
    languageCode: String = AppConfig.DEFAULT_LANGUAGE_CODE,
    themeMode: ThemeMode = ThemeMode.Auto,
    roadAlertsEnabled: Boolean = true,
    voiceGuidanceEnabled: Boolean = true,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    avoidTolls: Boolean = false,
    avoidFerries: Boolean = false,
    avoidUnpaved: Boolean = false,
    locationPermissionAsked: Boolean = false,
) : SettingsRepository {

    private val _onboardingCompleted = MutableStateFlow(onboardingCompleted)
    private val _languageCode = MutableStateFlow(languageCode)
    private val _themeMode = MutableStateFlow(themeMode)
    private val _roadAlertsEnabled = MutableStateFlow(roadAlertsEnabled)
    private val _voiceGuidanceEnabled = MutableStateFlow(voiceGuidanceEnabled)
    private val _distanceUnit = MutableStateFlow(distanceUnit)
    private val _avoidTolls = MutableStateFlow(avoidTolls)
    private val _avoidFerries = MutableStateFlow(avoidFerries)
    private val _avoidUnpaved = MutableStateFlow(avoidUnpaved)
    private val _locationPermissionAsked = MutableStateFlow(locationPermissionAsked)

    override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted.asStateFlow()
    override val selectedLanguageCode: Flow<String> = _languageCode.asStateFlow()
    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()
    override val roadAlertsEnabled: Flow<Boolean> = _roadAlertsEnabled.asStateFlow()
    override val voiceGuidanceEnabled: Flow<Boolean> = _voiceGuidanceEnabled.asStateFlow()
    override val distanceUnit: Flow<DistanceUnit> = _distanceUnit.asStateFlow()
    override val avoidTolls: Flow<Boolean> = _avoidTolls.asStateFlow()
    override val avoidFerries: Flow<Boolean> = _avoidFerries.asStateFlow()
    override val avoidUnpaved: Flow<Boolean> = _avoidUnpaved.asStateFlow()
    override val locationPermissionAsked: Flow<Boolean> = _locationPermissionAsked.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    override suspend fun setSelectedLanguageCode(code: String) {
        _languageCode.value = code
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    override suspend fun setRoadAlertsEnabled(enabled: Boolean) {
        _roadAlertsEnabled.value = enabled
    }

    override suspend fun setVoiceGuidanceEnabled(enabled: Boolean) {
        _voiceGuidanceEnabled.value = enabled
    }

    override suspend fun setDistanceUnit(unit: DistanceUnit) {
        _distanceUnit.value = unit
    }

    override suspend fun setAvoidTolls(avoid: Boolean) {
        _avoidTolls.value = avoid
    }

    override suspend fun setAvoidFerries(avoid: Boolean) {
        _avoidFerries.value = avoid
    }

    override suspend fun setAvoidUnpaved(avoid: Boolean) {
        _avoidUnpaved.value = avoid
    }

    override suspend fun setLocationPermissionAsked(asked: Boolean) {
        _locationPermissionAsked.value = asked
    }

    private val _cameraEmbedGpsExif = MutableStateFlow(true)
    override val cameraEmbedGpsExif: Flow<Boolean> = _cameraEmbedGpsExif.asStateFlow()

    override suspend fun setCameraEmbedGpsExif(enabled: Boolean) {
        _cameraEmbedGpsExif.value = enabled
    }

    fun currentCameraEmbedGpsExif(): Boolean = _cameraEmbedGpsExif.value

    fun currentOnboardingCompleted(): Boolean = _onboardingCompleted.value
    fun currentLocationPermissionAsked(): Boolean = _locationPermissionAsked.value
    fun currentThemeMode(): ThemeMode = _themeMode.value
    fun currentRoadAlertsEnabled(): Boolean = _roadAlertsEnabled.value
    fun currentVoiceGuidanceEnabled(): Boolean = _voiceGuidanceEnabled.value
    fun currentDistanceUnit(): DistanceUnit = _distanceUnit.value
}
