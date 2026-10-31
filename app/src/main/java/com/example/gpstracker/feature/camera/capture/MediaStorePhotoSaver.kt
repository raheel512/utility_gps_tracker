package com.example.gpstracker.feature.camera.capture

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class SavedPhoto(
    val uri: Uri,
    val displayName: String,
)

interface LatestPhotoQuery {
    fun latestPhotoUri(): Uri?
}

@Singleton
class MediaStorePhotoSaver @Inject constructor(
    @ApplicationContext private val context: Context,
) : LatestPhotoQuery {
    private val resolver: ContentResolver get() = context.contentResolver

    /**
     * Writes [bitmap] as JPEG (~[quality]) into Pictures/GPS Tracker using IS_PENDING on API 29+.
     */
    fun saveJpeg(
        bitmap: Bitmap,
        dateTime: LocalDateTime,
        quality: Int = 90,
        latitude: Double? = null,
        longitude: Double? = null,
        altitudeMeters: Double? = null,
        timestampMillis: Long? = null,
        embedGpsExif: Boolean = true,
    ): SavedPhoto {
        val name = GpsPhotoFileNames.forCapture(dateTime)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, GpsPhotoFileNames.MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, GpsPhotoFileNames.RELATIVE_PATH)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                @Suppress("DEPRECATION")
                put(
                    MediaStore.Images.Media.DATA,
                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/GPS Tracker/$name",
                )
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values)
            ?: throw IOException("MediaStore insert failed")
        try {
            resolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                resolver.openOutputStream(uri)?.use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                        throw IOException("JPEG compress failed")
                    }
                } ?: throw IOException("Cannot open output stream")
                ExifGps.write(
                    fileDescriptor = pfd.fileDescriptor,
                    latitude = latitude,
                    longitude = longitude,
                    altitudeMeters = altitudeMeters,
                    timestampMillis = timestampMillis,
                    embedGps = embedGpsExif,
                )
            } ?: throw IOException("Cannot open file descriptor")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return SavedPhoto(uri, name)
        } catch (t: Throwable) {
            runCatching { resolver.delete(uri, null, null) }
                .onFailure { Timber.e(it, "Could not delete failed MediaStore entry") }
            throw t
        }
    }

    /** Latest image in our MediaStore folder, or null. No READ_MEDIA permission required for our own inserts on API 29+. */
    override fun latestPhotoUri(): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sort = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val selection: String?
        val args: Array<String>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            args = arrayOf("%${GpsPhotoFileNames.RELATIVE_PATH}%")
        } else {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            args = arrayOf("%/GPS Tracker/%")
        }
        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            args,
            sort,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            }
        }
        return null
    }
}
