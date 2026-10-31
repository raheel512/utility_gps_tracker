package com.example.gpstracker.feature.camera.ui

import android.net.Uri
import com.example.gpstracker.R
import com.example.gpstracker.core.common.MainDispatcherRule
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.FakeSettingsRepository
import com.example.gpstracker.core.location.FakeLocationRepository
import com.example.gpstracker.core.location.geocode.FakeReverseGeocoder
import com.example.gpstracker.core.permissions.CameraPermissionState
import com.example.gpstracker.core.sensors.DeviceHeading
import com.example.gpstracker.core.sensors.GnssStatusRepository
import com.example.gpstracker.core.sensors.HeadingAccuracy
import com.example.gpstracker.core.sensors.HeadingProvider
import com.example.gpstracker.core.sensors.PressureProvider
import com.example.gpstracker.feature.camera.capture.LatestPhotoQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GpsCameraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val locations = FakeLocationRepository()
    private val geocoder = FakeReverseGeocoder()
    private val settings = FakeSettingsRepository()
    private val headingProvider = object : HeadingProvider {
        override fun headings(location: android.location.Location?): Flow<DeviceHeading> =
            flowOf(DeviceHeading(342f, "NNW", HeadingAccuracy.High))
    }
    private val pressureProvider = object : PressureProvider {
        override fun pressureHpa(): Flow<Float?> = flowOf(1012f)
    }
    private val gnss = object : GnssStatusRepository {
        override fun satellitesUsedInFix(): Flow<Int?> = flowOf(18)
    }
    private var latestUri: Uri? = null
    private val photos = object : LatestPhotoQuery {
        override fun latestPhotoUri(): Uri? = latestUri
    }

    private fun create() = GpsCameraViewModel(
        locationRepository = locations,
        reverseGeocoder = geocoder,
        settingsRepository = settings,
        headingProvider = headingProvider,
        pressureProvider = pressureProvider,
        gnssStatusRepository = gnss,
        latestPhotoQuery = photos,
    )

    @Test
    fun `flash menu opens and selecting a mode applies it and closes menu`() = runTest {
        val vm = create()
        assertEquals(FlashModeCycle.Auto, vm.uiState.value.flashMode)
        vm.onEvent(GpsCameraEvent.FlashClicked)
        assertTrue(vm.uiState.value.flashMenuOpen)
        vm.onEvent(GpsCameraEvent.FlashSelected(FlashModeCycle.Off))
        assertEquals(FlashModeCycle.Off, vm.uiState.value.flashMode)
        assertFalse(vm.uiState.value.flashMenuOpen)
        vm.onEvent(GpsCameraEvent.FlashClicked)
        vm.onEvent(GpsCameraEvent.FlashMenuDismissed)
        assertFalse(vm.uiState.value.flashMenuOpen)
        assertEquals(FlashModeCycle.Off, vm.uiState.value.flashMode)
    }

    @Test
    fun `grid toggles`() = runTest {
        val vm = create()
        assertTrue(vm.uiState.value.gridEnabled)
        vm.onEvent(GpsCameraEvent.ToggleGrid)
        assertFalse(vm.uiState.value.gridEnabled)
        vm.onEvent(GpsCameraEvent.ToggleGrid)
        assertTrue(vm.uiState.value.gridEnabled)
    }

    @Test
    fun `settings opens sheet and persists exif and unit choices`() = runTest {
        val vm = create()
        vm.onEvent(GpsCameraEvent.SettingsClicked)
        assertTrue(vm.uiState.value.settingsOpen)
        vm.onEvent(GpsCameraEvent.EmbedGpsExifChanged(false))
        vm.onEvent(GpsCameraEvent.DistanceUnitSelected(DistanceUnit.MI))
        advanceUntilIdle()
        assertFalse(settings.currentCameraEmbedGpsExif())
        assertFalse(vm.uiState.value.embedGpsExif)
        assertEquals(DistanceUnit.MI, settings.currentDistanceUnit())
        assertEquals(DistanceUnit.MI, vm.uiState.value.distanceUnit)
        vm.onEvent(GpsCameraEvent.SettingsDismissed)
        assertFalse(vm.uiState.value.settingsOpen)
    }

    @Test
    fun `session counter increments on capture success`() = runTest {
        val vm = create()
        vm.onCaptureFinished(success = true)
        assertEquals(1, vm.uiState.value.sessionPhotoCount)
        assertFalse(vm.uiState.value.isProcessing)
        vm.onCaptureFinished(success = true)
        assertEquals(2, vm.uiState.value.sessionPhotoCount)
    }

    @Test
    fun `capture failure emits message and clears processing`() = runTest {
        val vm = create()
        vm.onEvent(GpsCameraEvent.CameraPermissionChanged(CameraPermissionState.Granted))
        val effects = mutableListOf<GpsCameraEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(GpsCameraEvent.ShutterClicked)
        advanceUntilIdle()
        assertTrue(effects.any { it is GpsCameraEffect.PerformCapture })
        vm.onCaptureFinished(success = false)
        advanceUntilIdle()
        assertTrue(
            effects.any {
                it is GpsCameraEffect.ShowMessage && it.messageRes == R.string.gps_camera_capture_failed
            },
        )
        assertFalse(vm.uiState.value.isProcessing)
        assertEquals(0, vm.uiState.value.sessionPhotoCount)
    }

    @Test
    fun `shutter without camera permission requests it`() = runTest {
        val vm = create()
        val effects = mutableListOf<GpsCameraEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(GpsCameraEvent.ShutterClicked)
        advanceUntilIdle()
        assertEquals(listOf(GpsCameraEffect.RequestCameraPermission), effects)
    }

    @Test
    fun `gallery with no photos shows message and clears thumbnail`() = runTest {
        val vm = create()
        vm.onCaptureFinished(success = true)
        assertEquals(1, vm.uiState.value.sessionPhotoCount)
        latestUri = null
        val effects = mutableListOf<GpsCameraEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effects.collect { effects.add(it) }
        }
        vm.onEvent(GpsCameraEvent.GalleryClicked)
        advanceUntilIdle()
        assertTrue(
            effects.any {
                it is GpsCameraEffect.ShowMessage && it.messageRes == R.string.gps_camera_no_photos
            },
        )
        assertEquals(null, vm.uiState.value.latestPhotoUri)
        assertEquals(0, vm.uiState.value.sessionPhotoCount)
    }

    @Test
    fun `refresh clears stale thumbnail when MediaStore is empty`() = runTest {
        val vm = create()
        vm.onCaptureFinished(success = true)
        latestUri = null
        vm.refreshLatestPhoto()
        assertEquals(null, vm.uiState.value.latestPhotoUri)
        assertEquals(0, vm.uiState.value.sessionPhotoCount)
    }
}
