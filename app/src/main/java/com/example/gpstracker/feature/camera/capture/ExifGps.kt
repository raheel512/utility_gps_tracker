package com.example.gpstracker.feature.camera.capture

import androidx.exifinterface.media.ExifInterface
import java.io.FileDescriptor
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.floor

/**
 * EXIF GPS helpers. Rational conversion is unit-tested with hand-derived values.
 *
 * Flag: EXIF embedding is on by default; a Settings toggle can gate this later.
 */
object ExifGps {

    private val exifDateTime = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    /**
     * Degrees → EXIF rational string "d/1,m/1,s/10000" (seconds to 1e-4).
     *
     * Example lat 33.6938:
     * - degrees = floor(33.6938) = 33
     * - minutesFloat = (33.6938 - 33) * 60 = 41.628
     * - minutes = floor(41.628) = 41
     * - seconds = (41.628 - 41) * 60 = 37.68 → 376800 / 10000
     * → "33/1,41/1,376800/10000"
     */
    fun toExifLatLong(decimalDegrees: Double): String {
        val abs = abs(decimalDegrees)
        val degrees = floor(abs).toInt()
        val minutesFloat = (abs - degrees) * 60.0
        val minutes = floor(minutesFloat).toInt()
        val seconds = (minutesFloat - minutes) * 60.0
        val secondsScaled = Math.round(seconds * 10_000.0)
        return "$degrees/1,$minutes/1,$secondsScaled/10000"
    }

    fun latitudeRef(latitude: Double): String =
        if (latitude >= 0) ExifInterface.LATITUDE_NORTH else ExifInterface.LATITUDE_SOUTH

    fun longitudeRef(longitude: Double): String =
        if (longitude >= 0) ExifInterface.LONGITUDE_EAST else ExifInterface.LONGITUDE_WEST

    fun write(
        fileDescriptor: FileDescriptor,
        latitude: Double?,
        longitude: Double?,
        altitudeMeters: Double?,
        timestampMillis: Long?,
        embedGps: Boolean = true,
    ) {
        val exif = ExifInterface(fileDescriptor)
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
        if (timestampMillis != null) {
            val local = Instant.ofEpochMilli(timestampMillis).atZone(ZoneOffset.systemDefault())
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, local.format(exifDateTime))
            exif.setAttribute(ExifInterface.TAG_DATETIME, local.format(exifDateTime))
        }
        if (embedGps && latitude != null && longitude != null) {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, toExifLatLong(latitude))
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(latitude))
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, toExifLatLong(longitude))
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef(longitude))
            if (altitudeMeters != null) {
                exif.setAttribute(
                    ExifInterface.TAG_GPS_ALTITUDE,
                    "${Math.round(abs(altitudeMeters) * 100)}/100",
                )
                exif.setAttribute(
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    if (altitudeMeters >= 0) "0" else "1",
                )
            }
            if (timestampMillis != null) {
                val utc = Instant.ofEpochMilli(timestampMillis).atZone(ZoneOffset.UTC)
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, utc.toLocalDate().toString().replace('-', ':'))
                exif.setAttribute(
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    "%02d:%02d:%02d".format(utc.hour, utc.minute, utc.second),
                )
            }
        }
        exif.saveAttributes()
    }
}
