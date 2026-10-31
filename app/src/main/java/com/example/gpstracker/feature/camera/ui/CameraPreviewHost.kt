package com.example.gpstracker.feature.camera.ui

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gpstracker.core.common.rememberContentReady
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Composable
fun CameraPreviewHost(
    lensFacingFront: Boolean,
    flashMode: FlashModeCycle,
    enabled: Boolean,
    onFocusTap: (xFraction: Float, yFraction: Float) -> Unit,
    onImageCaptureReady: (ImageCapture?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val contentReady = rememberContentReady()

    LaunchedEffect(imageCapture) {
        onImageCaptureReady(imageCapture)
    }

    LaunchedEffect(flashMode, imageCapture, lensFacingFront) {
        imageCapture?.flashMode = when {
            lensFacingFront -> ImageCapture.FLASH_MODE_OFF
            flashMode == FlashModeCycle.Auto -> ImageCapture.FLASH_MODE_AUTO
            flashMode == FlashModeCycle.On -> ImageCapture.FLASH_MODE_ON
            else -> ImageCapture.FLASH_MODE_OFF
        }
    }

    DisposableEffect(lensFacingFront, enabled, contentReady, lifecycleOwner) {
        if (!enabled || !contentReady) {
            imageCapture = null
            camera = null
            onImageCaptureReady(null)
            onDispose { }
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            var unbound = false
            cameraProviderFuture.addListener(
                {
                    if (unbound) return@addListener
                    val provider = cameraProviderFuture.get()
                    provider.unbindAll()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setResolutionSelector(
                            ResolutionSelector.Builder()
                                .setAspectRatioStrategy(
                                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY,
                                )
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        android.util.Size(4032, 3024),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
                                    ),
                                )
                                .build(),
                        )
                        .build()
                    val selector = if (lensFacingFront) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    try {
                        val bound = provider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            capture,
                        )
                        camera = bound
                        imageCapture = capture
                    } catch (e: Exception) {
                        Timber.e(e, "Camera bind failed")
                        camera = null
                        imageCapture = null
                    }
                },
                executor,
            )
            onDispose {
                unbound = true
                // get() waits for the provider. Only call it after the future
                // has completed, or leaving the screen can freeze the UI.
                if (cameraProviderFuture.isDone) {
                    runCatching { cameraProviderFuture.get().unbindAll() }
                }
                imageCapture = null
                camera = null
            }
        }
    }

    AndroidView(
        factory = {
            val scaleDetector = ScaleGestureDetector(
                context,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val cam = camera ?: return false
                        val zoom = cam.cameraInfo.zoomState.value ?: return false
                        val newRatio = zoom.zoomRatio * detector.scaleFactor
                        cam.cameraControl.setZoomRatio(
                            newRatio.coerceIn(zoom.minZoomRatio, zoom.maxZoomRatio),
                        )
                        return true
                    }
                },
            )
            previewView.setOnTouchListener { view, event ->
                scaleDetector.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_UP && !scaleDetector.isInProgress) {
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(event.x, event.y)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    camera?.cameraControl?.startFocusAndMetering(action)
                    onFocusTap(event.x / view.width.coerceAtLeast(1), event.y / view.height.coerceAtLeast(1))
                    view.performClick()
                }
                true
            }
            previewView
        },
        modifier = modifier.fillMaxSize(),
    )
}
