package com.example.gpstracker.feature.camera.capture

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GpsPhotoFileNames {
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    /** e.g. GPS_20241014_102418.jpg */
    fun forCapture(dateTime: LocalDateTime): String =
        "GPS_${dateTime.format(formatter)}.jpg"

    const val RELATIVE_PATH = "Pictures/GPS Tracker"
    const val MIME_TYPE = "image/jpeg"
}
