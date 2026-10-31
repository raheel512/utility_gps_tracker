package com.example.gpstracker.core.location.format

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateFormatterTest {

    @Test
    fun `north and east use N and E`() {
        assertEquals("33.6938° N, 73.0489° E", CoordinateFormatter.format(33.6938, 73.0489))
    }

    @Test
    fun `negative values use S and W without a minus sign`() {
        assertEquals("33.8688° S, 151.2093° W", CoordinateFormatter.format(-33.8688, -151.2093))
    }

    @Test
    fun `mixed hemispheres`() {
        assertEquals("37.4221° N, 122.0841° W", CoordinateFormatter.format(37.4220699, -122.0841244))
        assertEquals("41.2731° S, 174.7859° E", CoordinateFormatter.format(-41.2730625, 174.7859375))
    }

    @Test
    fun `zero is N and E`() {
        assertEquals("0.0000° N, 0.0000° E", CoordinateFormatter.format(0.0, 0.0))
    }

    @Test
    fun `tiny negative values that round to zero are not S or W`() {
        assertEquals("0.0000° N, 0.0000° E", CoordinateFormatter.format(-0.00001, -0.00004))
    }

    @Test
    fun `rounds to four decimals`() {
        assertEquals("33.6938° N", CoordinateFormatter.formatLatitude(33.693849))
        assertEquals("33.6939° N", CoordinateFormatter.formatLatitude(33.693851))
        assertEquals("73.0490° E", CoordinateFormatter.formatLongitude(73.04899))
        assertEquals("90.0000° S", CoordinateFormatter.formatLatitude(-90.0))
        assertEquals("180.0000° W", CoordinateFormatter.formatLongitude(-180.0))
    }
}
