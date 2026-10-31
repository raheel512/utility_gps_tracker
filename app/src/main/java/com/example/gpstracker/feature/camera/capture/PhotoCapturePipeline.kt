package com.example.gpstracker.feature.camera.capture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.feature.camera.stamp.StampBitmapRenderer
import com.example.gpstracker.feature.camera.stamp.StampFormatter
import com.example.gpstracker.feature.camera.stamp.StampSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CaptureStampContext(
    val snapshot: StampSnapshot,
    val location: UserLocation?,
)

sealed interface CaptureResult {
    data class Success(val uri: Uri, val displayName: String) : CaptureResult
    data class Failure(val message: String, val cause: Throwable? = null) : CaptureResult
}

@Singleton
class PhotoCapturePipeline @Inject constructor(
    private val saver: MediaStorePhotoSaver,
    private val stampRenderer: StampBitmapRenderer,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun captureAndStamp(
        imageCapture: ImageCapture,
        executor: Executor,
        stampContext: CaptureStampContext,
        embedGpsExif: Boolean = true,
    ): CaptureResult = withContext(dispatchers.io) {
        try {
            val proxy = takePicture(imageCapture, executor)
            var bitmap: Bitmap? = null
            try {
                bitmap = try {
                    proxy.toBitmapNormalized()
                } finally {
                    proxy.close()
                }
                val model = StampFormatter.format(stampContext.snapshot)
                stampRenderer.drawOnto(bitmap, model)
                val now = LocalDateTime.now()
                val loc = stampContext.location
                val saved = saver.saveJpeg(
                    bitmap = bitmap,
                    dateTime = now,
                    quality = 90,
                    latitude = loc?.latitude,
                    longitude = loc?.longitude,
                    altitudeMeters = loc?.altitudeMeters,
                    timestampMillis = loc?.timestampMillis ?: System.currentTimeMillis(),
                    embedGpsExif = embedGpsExif,
                )
                CaptureResult.Success(saved.uri, saved.displayName)
            } finally {
                bitmap?.recycle()
            }
        } catch (t: Throwable) {
            Timber.e(t, "Photo capture failed")
            CaptureResult.Failure(t.message ?: "Capture failed", t)
        }
    }

    private suspend fun takePicture(
        imageCapture: ImageCapture,
        executor: Executor,
    ): ImageProxy = suspendCancellableCoroutine { cont ->
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    cont.resume(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            },
        )
    }
}

/**
 * Decode + rotate so the bitmap is upright; orientation EXIF will be set to normal later.
 * Result is mutable because [StampBitmapRenderer] draws onto it via a Canvas.
 */
internal fun ImageProxy.toBitmapNormalized(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val options = BitmapFactory.Options().apply { inMutable = true }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        ?: error("Failed to decode capture")
    val rotation = imageInfo.rotationDegrees
    val upright = if (rotation == 0) {
        decoded
    } else {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            .also { if (it != decoded) decoded.recycle() }
    }
    if (upright.isMutable) return upright
    // createBitmap with a matrix returns an immutable bitmap.
    val mutable = upright.copy(Bitmap.Config.ARGB_8888, true)
        ?: error("Failed to copy capture to a mutable bitmap")
    upright.recycle()
    return mutable
}

fun stampSnapshotNow(
    zoneId: ZoneId = ZoneId.systemDefault(),
    addressLine: String?,
    location: UserLocation?,
    distanceUnit: com.example.gpstracker.core.data.DistanceUnit,
    pressureHpa: Float?,
    headingDegrees: Float?,
    headingCardinal: String?,
): StampSnapshot {
    val zdt = Instant.now().atZone(zoneId)
    return StampSnapshot(
        dateTime = zdt,
        zoneId = zoneId,
        addressLine = addressLine,
        latitude = location?.latitude,
        longitude = location?.longitude,
        altitudeMeters = location?.altitudeMeters,
        speedMps = location?.speedMps,
        distanceUnit = distanceUnit,
        pressureHpa = pressureHpa,
        headingDegrees = headingDegrees,
        headingCardinal = headingCardinal,
    )
}
