package com.example.gpstracker.feature.camera.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class ExifGpsTest {

    @Test
    fun `toExifLatLong for 33_6938`() {
        // degrees = floor(33.6938) = 33
        // minutesFloat = (33.6938 - 33) * 60 = 41.628
        // minutes = floor(41.628) = 41
        // seconds = (41.628 - 41) * 60 = 37.68
        // secondsScaled = round(37.68 * 10000) = 376800
        assertEquals("33/1,41/1,376800/10000", ExifGps.toExifLatLong(33.6938))
    }

    @Test
    fun `toExifLatLong for negative longitude uses absolute magnitude`() {
        // abs(-73.0489) = 73.0489
        // degrees = 73
        // minutesFloat = 0.0489 * 60 = 2.934
        // minutes = 2
        // seconds = 0.934 * 60 = 56.04
        // secondsScaled = round(56.04 * 10000) = 560400
        assertEquals("73/1,2/1,560400/10000", ExifGps.toExifLatLong(-73.0489))
    }

    @Test
    fun `latitude and longitude refs`() {
        assertEquals("N", ExifGps.latitudeRef(33.0))
        assertEquals("S", ExifGps.latitudeRef(-33.0))
        assertEquals("E", ExifGps.longitudeRef(73.0))
        assertEquals("W", ExifGps.longitudeRef(-73.0))
    }

    @Test
    fun `zero latitude is North`() {
        assertEquals("N", ExifGps.latitudeRef(0.0))
        assertEquals("E", ExifGps.longitudeRef(0.0))
    }
}
