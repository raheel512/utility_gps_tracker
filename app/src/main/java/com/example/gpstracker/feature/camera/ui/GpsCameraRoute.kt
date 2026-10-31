package com.example.gpstracker.feature.camera.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.R
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.location.gate.LocationGate
import com.example.gpstracker.core.location.gate.LocationGatePhase
import com.example.gpstracker.core.location.gate.LocationGateViewModel
import com.example.gpstracker.core.permissions.CameraPermissionState
import com.example.gpstracker.core.permissions.currentCameraPermissionState
import com.example.gpstracker.core.permissions.openApplicationDetailsSettings
import com.example.gpstracker.core.permissions.rememberCameraPermissionLauncher
import com.example.gpstracker.feature.camera.capture.CaptureResult
import com.example.gpstracker.feature.camera.capture.PhotoCapturePipeline
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CapturePipelineEntryPoint {
    fun photoCapturePipeline(): PhotoCapturePipeline
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun GpsCameraRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GpsCameraViewModel = hiltViewModel(),
    locationGateViewModel: LocationGateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gateState by locationGateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraAsked by remember { mutableStateOf(false) }
    var showCameraRationale by remember { mutableStateOf(false) }

    val pipeline = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            CapturePipelineEntryPoint::class.java,
        ).photoCapturePipeline()
    }

    LaunchedEffect(gateState.phase) {
        viewModel.setLocationUpdatesEnabled(gateState.phase == LocationGatePhase.Ready)
    }

    // Photos may be deleted in the system gallery while we are away — refresh thumbnail.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshLatestPhoto()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestCamera = rememberCameraPermissionLauncher(
        onMarkAsked = { cameraAsked = true },
        onResult = { state ->
            viewModel.onEvent(GpsCameraEvent.CameraPermissionChanged(state))
            showCameraRationale = state == CameraPermissionState.PermanentlyDenied ||
                state == CameraPermissionState.Denied
        },
    )

    // WRITE_EXTERNAL_STORAGE only on API ≤ 28, only when saving. Copy not in design if denied.
    var storageGrantContinuation by remember {
        mutableStateOf<((Boolean) -> Unit)?>(null)
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        storageGrantContinuation?.invoke(granted)
        storageGrantContinuation = null
    }

    suspend fun ensureLegacyStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) return true
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return true
        return suspendCancellableCoroutine { cont ->
            storageGrantContinuation = { ok -> cont.resume(ok) }
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(Unit) {
        val state = context.currentCameraPermissionState(hasAsked = cameraAsked)
        viewModel.onEvent(GpsCameraEvent.CameraPermissionChanged(state))
        when (state) {
            CameraPermissionState.Granted -> Unit
            CameraPermissionState.NotDetermined -> requestCamera()
            CameraPermissionState.Denied,
            CameraPermissionState.PermanentlyDenied,
            -> showCameraRationale = true
        }
    }

    DisposableEffect(imageCapture) {
        val capture = imageCapture
        if (capture == null) {
            onDispose { }
        } else {
            val listener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return
                    capture.targetRotation = when (orientation) {
                        in 45 until 135 -> Surface.ROTATION_270
                        in 135 until 225 -> Surface.ROTATION_180
                        in 225 until 315 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                }
            }
            if (listener.canDetectOrientation()) listener.enable()
            onDispose { listener.disable() }
        }
    }

    // Camera preview is full-bleed and dark: force light system-bar icons while visible.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val lightStatus = controller?.isAppearanceLightStatusBars
        val lightNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            lightStatus?.let { controller.isAppearanceLightStatusBars = it }
            lightNav?.let { controller.isAppearanceLightNavigationBars = it }
        }
    }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            GpsCameraEffect.RequestCameraPermission -> requestCamera()
            GpsCameraEffect.OpenAppSettings -> context.openApplicationDetailsSettings()
            is GpsCameraEffect.OpenGalleryView -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(effect.uri, "image/jpeg")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching { context.startActivity(intent) }
                    .onFailure {
                        Timber.e(it, "Could not open photo viewer")
                        viewModel.onGalleryOpenFailed()
                    }
            }
            is GpsCameraEffect.ShowMessage -> {
                scope.launch { snackbar.showSnackbar(context.getString(effect.messageRes)) }
            }
            GpsCameraEffect.CaptureHaptic -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            GpsCameraEffect.PerformCapture -> {
                val capture = imageCapture
                if (capture == null) {
                    viewModel.onCaptureFinished(success = false)
                } else {
                    scope.launch {
                        try {
                            if (!ensureLegacyStoragePermission()) {
                                // copy not in design
                                snackbar.showSnackbar(context.getString(R.string.gps_camera_storage_denied))
                                viewModel.onCaptureCancelled()
                                return@launch
                            }
                            when (
                                val result = pipeline.captureAndStamp(
                                    imageCapture = capture,
                                    executor = ContextCompat.getMainExecutor(context),
                                    stampContext = viewModel.captureStampContext(),
                                    embedGpsExif = viewModel.uiState.value.embedGpsExif,
                                )
                            ) {
                                is CaptureResult.Success -> {
                                    viewModel.onCaptureFinished(success = true, uri = result.uri)
                                }
                                is CaptureResult.Failure -> {
                                    viewModel.onCaptureFinished(success = false)
                                }
                            }
                        } finally {
                            // Covers cancellation (e.g. leaving the screen mid-capture).
                            if (viewModel.uiState.value.isProcessing) viewModel.onCaptureCancelled()
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Soft location: content always shown; Not Now does not leave. Design review.
        LocationGate(
            requiresNetwork = false,
            softMode = true,
            onDismiss = { },
            viewModel = locationGateViewModel,
        ) {
            GpsCameraScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onImageCaptureReady = { imageCapture = it },
            )
        }

        // Camera permission — copy not in design.
        if (showCameraRationale && uiState.cameraPermission != CameraPermissionState.Granted) {
            val permanent = uiState.cameraPermission == CameraPermissionState.PermanentlyDenied
            AlertDialog(
                onDismissRequest = {
                    showCameraRationale = false
                    if (uiState.cameraPermission != CameraPermissionState.Granted) onNavigateUp()
                },
                title = { Text(stringResource(R.string.camera_permission_title)) },
                text = { Text(stringResource(R.string.camera_permission_body)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCameraRationale = false
                            if (permanent) {
                                viewModel.onEvent(GpsCameraEvent.OpenAppSettings)
                            } else {
                                viewModel.onEvent(GpsCameraEvent.RequestCameraPermission)
                            }
                        },
                    ) {
                        Text(
                            stringResource(
                                if (permanent) {
                                    R.string.location_permission_open_settings
                                } else {
                                    R.string.camera_permission_allow
                                },
                            ),
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCameraRationale = false
                            onNavigateUp()
                        },
                    ) {
                        Text(stringResource(R.string.camera_permission_not_now))
                    }
                },
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        )
    }
}
