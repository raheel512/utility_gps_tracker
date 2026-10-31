package com.example.gpstracker.core.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

sealed interface CameraPermissionState {
    data object Granted : CameraPermissionState
    data object Denied : CameraPermissionState
    data object PermanentlyDenied : CameraPermissionState
    data object NotDetermined : CameraPermissionState
}

fun resolveCameraPermissionState(
    granted: Boolean,
    shouldShowRationale: Boolean,
    hasAsked: Boolean,
): CameraPermissionState = when {
    granted -> CameraPermissionState.Granted
    !hasAsked -> CameraPermissionState.NotDetermined
    shouldShowRationale -> CameraPermissionState.Denied
    else -> CameraPermissionState.PermanentlyDenied
}

fun Context.isCameraPermissionGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

fun Activity.shouldShowCameraRationale(): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)

fun Context.currentCameraPermissionState(hasAsked: Boolean): CameraPermissionState {
    val granted = isCameraPermissionGranted()
    val activity = this as? Activity
    val showRationale = activity?.shouldShowCameraRationale() ?: false
    return resolveCameraPermissionState(granted, showRationale, hasAsked)
}

@Composable
fun rememberCameraPermissionLauncher(
    onMarkAsked: () -> Unit,
    onResult: (CameraPermissionState) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val activity = context as? Activity
        val showRationale = activity?.shouldShowCameraRationale() ?: false
        onResult(
            resolveCameraPermissionState(
                granted = granted || context.isCameraPermissionGranted(),
                shouldShowRationale = showRationale,
                hasAsked = true,
            ),
        )
    }
    return remember(launcher, onMarkAsked) {
        {
            onMarkAsked()
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}
