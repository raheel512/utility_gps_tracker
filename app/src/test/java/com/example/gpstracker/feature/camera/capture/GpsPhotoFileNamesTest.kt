package com.example.gpstracker.feature.camera.capture

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GpsPhotoFileNamesTest {

    @Test
    fun `generates GPS_yyyyMMdd_HHmmss jpg`() {
        val name = GpsPhotoFileNames.forCapture(LocalDateTime.of(2024, 10, 14, 10, 24, 18))
        assertEquals("GPS_20241014_102418.jpg", name)
    }

    @Test
    fun `pads single-digit month and day`() {
        val name = GpsPhotoFileNames.forCapture(LocalDateTime.of(2024, 1, 2, 3, 4, 5))
        assertEquals("GPS_20240102_030405.jpg", name)
    }
}
