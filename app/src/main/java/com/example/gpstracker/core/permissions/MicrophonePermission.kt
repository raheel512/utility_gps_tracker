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

sealed interface MicrophonePermissionState {
    data object Granted : MicrophonePermissionState
    data object Denied : MicrophonePermissionState
    data object PermanentlyDenied : MicrophonePermissionState
    data object NotDetermined : MicrophonePermissionState
}

fun resolveMicrophonePermissionState(
    granted: Boolean,
    shouldShowRationale: Boolean,
    hasAsked: Boolean,
): MicrophonePermissionState = when {
    granted -> MicrophonePermissionState.Granted
    !hasAsked -> MicrophonePermissionState.NotDetermined
    shouldShowRationale -> MicrophonePermissionState.Denied
    else -> MicrophonePermissionState.PermanentlyDenied
}

fun Context.isMicrophonePermissionGranted(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

fun Activity.shouldShowMicrophoneRationale(): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)

fun Context.currentMicrophonePermissionState(hasAsked: Boolean): MicrophonePermissionState {
    val granted = isMicrophonePermissionGranted()
    val activity = this as? Activity
    val showRationale = activity?.shouldShowMicrophoneRationale() ?: false
    return resolveMicrophonePermissionState(granted, showRationale, hasAsked)
}

@Composable
fun rememberMicrophonePermissionLauncher(
    onMarkAsked: () -> Unit,
    onResult: (MicrophonePermissionState) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val activity = context as? Activity
        val showRationale = activity?.shouldShowMicrophoneRationale() ?: false
        onResult(
            resolveMicrophonePermissionState(
                granted = granted || context.isMicrophonePermissionGranted(),
                shouldShowRationale = showRationale,
                hasAsked = true,
            ),
        )
    }
    return remember(launcher, onMarkAsked) {
        {
            onMarkAsked()
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
