package com.example.gpstracker.feature.camera.ui

import android.location.Location
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.R
import com.example.gpstracker.core.common.EventBus
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.location.LocationRepository
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.example.gpstracker.core.permissions.CameraPermissionState
import com.example.gpstracker.core.sensors.DeviceHeading
import com.example.gpstracker.core.sensors.GnssStatusRepository
import com.example.gpstracker.core.sensors.HeadingAccuracy
import com.example.gpstracker.core.sensors.HeadingProvider
import com.example.gpstracker.core.sensors.PressureProvider
import com.example.gpstracker.feature.camera.capture.CaptureStampContext
import com.example.gpstracker.feature.camera.capture.LatestPhotoQuery
import com.example.gpstracker.feature.camera.capture.stampSnapshotNow
import com.example.gpstracker.feature.camera.stamp.StampFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GpsCameraViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val reverseGeocoder: ReverseGeocoder,
    private val settingsRepository: SettingsRepository,
    private val headingProvider: HeadingProvider,
    private val pressureProvider: PressureProvider,
    private val gnssStatusRepository: GnssStatusRepository,
    private val latestPhotoQuery: LatestPhotoQuery,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GpsCameraUiState(latestPhotoUri = latestPhotoQuery.latestPhotoUri()),
    )
    val uiState: StateFlow<GpsCameraUiState> = _uiState.asStateFlow()

    private val effectBus = EventBus<GpsCameraEffect>()
    val effects: Flow<GpsCameraEffect> = effectBus.events

    private val locationEnabled = MutableStateFlow(false)
    private val distanceUnit = MutableStateFlow(DistanceUnit.KM)
    private val locationFlow = MutableStateFlow<UserLocation?>(null)

    private var addressLine: String? = null
    private var heading: DeviceHeading? = null
    private var pressureHpa: Float? = null
    private var satellites: Int? = null
    private var focusClearJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.distanceUnit.collect { unit ->
                distanceUnit.value = unit
                _uiState.update { it.copy(distanceUnit = unit) }
                publishStamp()
            }
        }
        viewModelScope.launch {
            settingsRepository.cameraEmbedGpsExif.collect { enabled ->
                _uiState.update { it.copy(embedGpsExif = enabled) }
            }
        }
        viewModelScope.launch {
            locationEnabled.flatMapLatest { enabled ->
                if (!enabled) flowOf(null as UserLocation?)
                else flow {
                    emit(locationRepository.getCurrentLocation())
                    emitAll(locationRepository.locationUpdates())
                }
            }.collect {
                locationFlow.value = it
                publishStamp()
            }
        }
        viewModelScope.launch {
            locationEnabled.flatMapLatest { enabled ->
                if (!enabled) flowOf(null as String?)
                else flow {
                    emit(null)
                    var lastKey: Pair<Long, Long>? = null
                    locationRepository.getCurrentLocation()?.let { first ->
                        lastKey = first.roundedKey()
                        emit(
                            reverseGeocoder.reverseGeocode(first.latitude, first.longitude)
                                ?.displayLine(),
                        )
                    }
                    locationRepository.locationUpdates()
                        .distinctUntilChangedBy { it.roundedKey() }
                        .collect { loc ->
                            if (loc.roundedKey() != lastKey) {
                                lastKey = loc.roundedKey()
                                emit(
                                    reverseGeocoder.reverseGeocode(loc.latitude, loc.longitude)
                                        ?.displayLine(),
                                )
                            }
                        }
                }
            }.collect {
                addressLine = it
                publishStamp()
            }
        }
        viewModelScope.launch {
            locationEnabled.flatMapLatest { enabled ->
                if (!enabled) flowOf(null as DeviceHeading?)
                else headingProvider.headings(locationFlow.value?.toAndroidLocation())
            }.collect {
                heading = it
                publishStamp()
            }
        }
        viewModelScope.launch {
            pressureProvider.pressureHpa().collect {
                pressureHpa = it
                publishStamp()
            }
        }
        viewModelScope.launch {
            locationEnabled.flatMapLatest { enabled ->
                if (!enabled) flowOf(null as Int?)
                else gnssStatusRepository.satellitesUsedInFix()
            }.collect {
                satellites = it
                publishStamp()
            }
        }
    }

    fun setLocationUpdatesEnabled(enabled: Boolean) {
        locationEnabled.value = enabled
    }

    fun onEvent(event: GpsCameraEvent) {
        when (event) {
            is GpsCameraEvent.CameraPermissionChanged -> {
                _uiState.update { it.copy(cameraPermission = event.state) }
            }
            GpsCameraEvent.RequestCameraPermission -> {
                effectBus.tryEmit(GpsCameraEffect.RequestCameraPermission)
            }
            GpsCameraEvent.OpenAppSettings -> effectBus.tryEmit(GpsCameraEffect.OpenAppSettings)
            GpsCameraEvent.ToggleLens -> {
                _uiState.update { it.copy(lensFacingFront = !it.lensFacingFront) }
            }
            GpsCameraEvent.FlashClicked -> {
                _uiState.update { it.copy(flashMenuOpen = !it.flashMenuOpen) }
            }
            is GpsCameraEvent.FlashSelected -> {
                _uiState.update { it.copy(flashMode = event.mode, flashMenuOpen = false) }
            }
            GpsCameraEvent.FlashMenuDismissed -> {
                _uiState.update { it.copy(flashMenuOpen = false) }
            }
            GpsCameraEvent.ToggleGrid -> {
                _uiState.update { it.copy(gridEnabled = !it.gridEnabled) }
            }
            GpsCameraEvent.ShutterClicked -> {
                val state = _uiState.value
                if (state.isProcessing) return
                if (state.cameraPermission != CameraPermissionState.Granted) {
                    effectBus.tryEmit(GpsCameraEffect.RequestCameraPermission)
                    return
                }
                _uiState.update { it.copy(isProcessing = true) }
                effectBus.tryEmit(GpsCameraEffect.CaptureHaptic)
                effectBus.tryEmit(GpsCameraEffect.PerformCapture)
            }
            GpsCameraEvent.GalleryClicked -> {
                // Re-query MediaStore: photos may have been deleted outside the app.
                val uri = refreshLatestPhotoUri()
                if (uri != null) {
                    effectBus.tryEmit(GpsCameraEffect.OpenGalleryView(uri))
                } else {
                    effectBus.tryEmit(GpsCameraEffect.ShowMessage(R.string.gps_camera_no_photos))
                }
            }
            GpsCameraEvent.LatestPhotoUnavailable -> clearLatestPhoto()
            GpsCameraEvent.SettingsClicked -> {
                _uiState.update { it.copy(settingsOpen = true, flashMenuOpen = false) }
            }
            GpsCameraEvent.SettingsDismissed -> {
                _uiState.update { it.copy(settingsOpen = false) }
            }
            is GpsCameraEvent.EmbedGpsExifChanged -> {
                _uiState.update { it.copy(embedGpsExif = event.enabled) }
                viewModelScope.launch { settingsRepository.setCameraEmbedGpsExif(event.enabled) }
            }
            is GpsCameraEvent.DistanceUnitSelected -> {
                viewModelScope.launch { settingsRepository.setDistanceUnit(event.unit) }
            }
            GpsCameraEvent.TemplateClicked -> {
                // TODO(route): stamp template switcher
            }
            is GpsCameraEvent.FocusTapped -> {
                _uiState.update {
                    it.copy(focusPoint = FocusPoint(event.xFraction, event.yFraction))
                }
                focusClearJob?.cancel()
                focusClearJob = viewModelScope.launch {
                    delay(1_500)
                    _uiState.update { it.copy(focusPoint = null) }
                }
            }
            GpsCameraEvent.ClearFocusIndicator -> {
                _uiState.update { it.copy(focusPoint = null) }
            }
        }
    }

    fun captureStampContext(): CaptureStampContext = CaptureStampContext(
        snapshot = stampSnapshotNow(
            zoneId = ZoneId.systemDefault(),
            addressLine = addressLine,
            location = locationFlow.value,
            distanceUnit = distanceUnit.value,
            pressureHpa = pressureHpa,
            headingDegrees = heading?.degreesTrue,
            headingCardinal = heading?.cardinal,
        ),
        location = locationFlow.value,
    )

    fun onCaptureFinished(success: Boolean, uri: Uri? = null) {
        _uiState.update { state ->
            state.copy(
                isProcessing = false,
                latestPhotoUri = uri ?: state.latestPhotoUri,
                sessionPhotoCount = if (success) state.sessionPhotoCount + 1 else state.sessionPhotoCount,
            )
        }
        if (!success) {
            effectBus.tryEmit(GpsCameraEffect.ShowMessage(R.string.gps_camera_capture_failed))
        }
    }

    fun onCaptureCancelled() {
        _uiState.update { it.copy(isProcessing = false) }
    }

    /** Call when the camera screen resumes so a deleted gallery photo clears the thumbnail. */
    fun refreshLatestPhoto() {
        refreshLatestPhotoUri()
    }

    fun onGalleryOpenFailed() {
        clearLatestPhoto()
        effectBus.tryEmit(GpsCameraEffect.ShowMessage(R.string.gps_camera_no_photos))
    }

    private fun refreshLatestPhotoUri(): Uri? {
        val uri = latestPhotoQuery.latestPhotoUri()
        _uiState.update { state ->
            state.copy(
                latestPhotoUri = uri,
                sessionPhotoCount = if (uri == null) 0 else state.sessionPhotoCount,
            )
        }
        return uri
    }

    private fun clearLatestPhoto() {
        _uiState.update { it.copy(latestPhotoUri = null, sessionPhotoCount = 0) }
    }

    private fun publishStamp() {
        val loc = locationFlow.value
        val stamp = StampFormatter.format(
            stampSnapshotNow(
                zoneId = ZoneId.systemDefault(),
                addressLine = addressLine,
                location = loc,
                distanceUnit = distanceUnit.value,
                pressureHpa = pressureHpa,
                headingDegrees = heading?.degreesTrue,
                headingCardinal = heading?.cardinal,
            ),
        )
        _uiState.update {
            it.copy(
                stamp = stamp,
                satelliteCount = satellites,
                accuracyLine = loc?.let { l -> StampFormatter.formatAccuracyMeters(l.accuracyMeters) },
                headingLine = stamp.headingLine,
                headingAccurate = heading?.accuracy == HeadingAccuracy.High ||
                    heading?.accuracy == HeadingAccuracy.Medium,
            )
        }
    }

    private fun UserLocation.roundedKey(): Pair<Long, Long> =
        (latitude * 10_000).roundToLong() to (longitude * 10_000).roundToLong()

    private fun UserLocation.toAndroidLocation(): Location =
        Location("").also {
            it.latitude = latitude
            it.longitude = longitude
            it.altitude = altitudeMeters ?: 0.0
        }
}
