package com.example.gpstracker.core.data

import kotlinx.coroutines.flow.Flow

/**
 * App settings contract backed by DataStore Preferences.
 */
interface SettingsRepository {
    val onboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    /** ISO language code (e.g. "en"). Applied by [com.example.gpstracker.core.locale.AppLocale]. */
    val selectedLanguageCode: Flow<String>
    suspend fun setSelectedLanguageCode(code: String)

    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    val roadAlertsEnabled: Flow<Boolean>
    suspend fun setRoadAlertsEnabled(enabled: Boolean)

    val voiceGuidanceEnabled: Flow<Boolean>
    suspend fun setVoiceGuidanceEnabled(enabled: Boolean)

    val distanceUnit: Flow<DistanceUnit>
    suspend fun setDistanceUnit(unit: DistanceUnit)

    val avoidTolls: Flow<Boolean>
    suspend fun setAvoidTolls(avoid: Boolean)

    val avoidFerries: Flow<Boolean>
    suspend fun setAvoidFerries(avoid: Boolean)

    val avoidUnpaved: Flow<Boolean>
    suspend fun setAvoidUnpaved(avoid: Boolean)

    /** True after the app has requested runtime location permission at least once. */
    val locationPermissionAsked: Flow<Boolean>
    suspend fun setLocationPermissionAsked(asked: Boolean)

    /** GPS Camera: write latitude/longitude/altitude into the photo's EXIF. */
    val cameraEmbedGpsExif: Flow<Boolean>
    suspend fun setCameraEmbedGpsExif(enabled: Boolean)
}
