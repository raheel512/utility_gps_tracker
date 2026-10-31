package com.example.gpstracker.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gpstracker.core.common.AppConfig
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : SettingsRepository {

    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val languageCodeKey = stringPreferencesKey("selected_language_code")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val roadAlertsEnabledKey = booleanPreferencesKey("road_alerts_enabled")
    private val voiceGuidanceEnabledKey = booleanPreferencesKey("voice_guidance_enabled")
    private val distanceUnitKey = stringPreferencesKey("distance_unit")
    private val avoidTollsKey = booleanPreferencesKey("avoid_tolls")
    private val avoidFerriesKey = booleanPreferencesKey("avoid_ferries")
    private val avoidUnpavedKey = booleanPreferencesKey("avoid_unpaved")
    private val locationPermissionAskedKey = booleanPreferencesKey("location_permission_asked")
    private val cameraEmbedGpsExifKey = booleanPreferencesKey("camera_embed_gps_exif")

    override val onboardingCompleted: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[onboardingCompletedKey] ?: false }
            .flowOn(dispatchers.io)

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[onboardingCompletedKey] = completed
            }
        }
    }

    override val selectedLanguageCode: Flow<String> =
        context.settingsDataStore.data
            .map { prefs -> prefs[languageCodeKey] ?: AppConfig.DEFAULT_LANGUAGE_CODE }
            .flowOn(dispatchers.io)

    override suspend fun setSelectedLanguageCode(code: String) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[languageCodeKey] = code
            }
        }
    }

    override val themeMode: Flow<ThemeMode> =
        context.settingsDataStore.data
            .map { prefs -> ThemeMode.fromStorage(prefs[themeModeKey]) }
            .flowOn(dispatchers.io)

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[themeModeKey] = mode.name
            }
        }
    }

    override val roadAlertsEnabled: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[roadAlertsEnabledKey] ?: true }
            .flowOn(dispatchers.io)

    override suspend fun setRoadAlertsEnabled(enabled: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[roadAlertsEnabledKey] = enabled
            }
        }
    }

    override val voiceGuidanceEnabled: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[voiceGuidanceEnabledKey] ?: true }
            .flowOn(dispatchers.io)

    override suspend fun setVoiceGuidanceEnabled(enabled: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[voiceGuidanceEnabledKey] = enabled
            }
        }
    }

    override val distanceUnit: Flow<DistanceUnit> =
        context.settingsDataStore.data
            .map { prefs -> DistanceUnit.fromStorage(prefs[distanceUnitKey]) }
            .flowOn(dispatchers.io)

    override suspend fun setDistanceUnit(unit: DistanceUnit) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[distanceUnitKey] = unit.name
            }
        }
    }

    override val avoidTolls: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[avoidTollsKey] ?: false }
            .flowOn(dispatchers.io)

    override suspend fun setAvoidTolls(avoid: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[avoidTollsKey] = avoid
            }
        }
    }

    override val avoidFerries: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[avoidFerriesKey] ?: false }
            .flowOn(dispatchers.io)

    override suspend fun setAvoidFerries(avoid: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[avoidFerriesKey] = avoid
            }
        }
    }

    override val avoidUnpaved: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[avoidUnpavedKey] ?: false }
            .flowOn(dispatchers.io)

    override suspend fun setAvoidUnpaved(avoid: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[avoidUnpavedKey] = avoid
            }
        }
    }

    override val locationPermissionAsked: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[locationPermissionAskedKey] ?: false }
            .flowOn(dispatchers.io)

    override suspend fun setLocationPermissionAsked(asked: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[locationPermissionAskedKey] = asked
            }
        }
    }

    override val cameraEmbedGpsExif: Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[cameraEmbedGpsExifKey] ?: true }
            .flowOn(dispatchers.io)

    override suspend fun setCameraEmbedGpsExif(enabled: Boolean) {
        withContext(dispatchers.io) {
            context.settingsDataStore.edit { prefs ->
                prefs[cameraEmbedGpsExifKey] = enabled
            }
        }
    }
}
