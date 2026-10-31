package com.example.gpstracker.feature.camera.ui

import android.net.Uri
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.permissions.CameraPermissionState
import com.example.gpstracker.feature.camera.stamp.StampRenderModel

enum class FlashModeCycle { Auto, On, Off }

data class GpsCameraUiState(
    val cameraPermission: CameraPermissionState = CameraPermissionState.NotDetermined,
    val lensFacingFront: Boolean = false,
    val flashMode: FlashModeCycle = FlashModeCycle.Auto,
    val flashMenuOpen: Boolean = false,
    val gridEnabled: Boolean = true,
    val settingsOpen: Boolean = false,
    val embedGpsExif: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val isProcessing: Boolean = false,
    val sessionPhotoCount: Int = 0,
    val latestPhotoUri: Uri? = null,
    val satelliteCount: Int? = null,
    val accuracyLine: String? = null,
    val headingLine: String? = null,
    val headingAccurate: Boolean = false,
    val stamp: StampRenderModel? = null,
    val focusPoint: FocusPoint? = null,
)

data class FocusPoint(val xFraction: Float, val yFraction: Float)

sealed interface GpsCameraEvent {
    data class CameraPermissionChanged(val state: CameraPermissionState) : GpsCameraEvent
    data object RequestCameraPermission : GpsCameraEvent
    data object OpenAppSettings : GpsCameraEvent
    data object ToggleLens : GpsCameraEvent
    data object FlashClicked : GpsCameraEvent
    data class FlashSelected(val mode: FlashModeCycle) : GpsCameraEvent
    data object FlashMenuDismissed : GpsCameraEvent
    data object ToggleGrid : GpsCameraEvent
    data object ShutterClicked : GpsCameraEvent
    data object GalleryClicked : GpsCameraEvent
    /** Coil could not load [latestPhotoUri] — treat as deleted. */
    data object LatestPhotoUnavailable : GpsCameraEvent
    data object SettingsClicked : GpsCameraEvent
    data object SettingsDismissed : GpsCameraEvent
    data class EmbedGpsExifChanged(val enabled: Boolean) : GpsCameraEvent
    data class DistanceUnitSelected(val unit: DistanceUnit) : GpsCameraEvent
    data object TemplateClicked : GpsCameraEvent
    data class FocusTapped(val xFraction: Float, val yFraction: Float) : GpsCameraEvent
    data object ClearFocusIndicator : GpsCameraEvent
}

sealed interface GpsCameraEffect {
    data object RequestCameraPermission : GpsCameraEffect
    data object OpenAppSettings : GpsCameraEffect
    data class OpenGalleryView(val uri: Uri) : GpsCameraEffect
    data class ShowMessage(val messageRes: Int) : GpsCameraEffect
    data object CaptureHaptic : GpsCameraEffect
    /** UI layer binds CameraX and runs [PhotoCapturePipeline]. */
    data object PerformCapture : GpsCameraEffect
}
