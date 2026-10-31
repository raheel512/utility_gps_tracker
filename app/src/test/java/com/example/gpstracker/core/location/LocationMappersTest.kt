package com.example.gpstracker.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationMappersTest {

    @Test
    fun `maps core fields and optional absences`() {
        val user = mapToUserLocation(
            latitude = 33.6844,
            longitude = 73.0479,
            accuracyMeters = 8.5f,
            speedMps = null,
            altitudeMeters = null,
            bearingDegrees = null,
            timestampMillis = 1_700_000_000_000L,
        )

        assertEquals(33.6844, user.latitude, 0.0001)
        assertEquals(73.0479, user.longitude, 0.0001)
        assertEquals(8.5f, user.accuracyMeters, 0.01f)
        assertEquals(1_700_000_000_000L, user.timestampMillis)
        assertNull(user.speedMps)
        assertNull(user.altitudeMeters)
        assertNull(user.bearingDegrees)
    }

    @Test
    fun `maps optional fields when present`() {
        val user = mapToUserLocation(
            latitude = 1.0,
            longitude = 2.0,
            accuracyMeters = 3f,
            speedMps = 4.5f,
            altitudeMeters = 100.0,
            bearingDegrees = 90f,
            timestampMillis = 99L,
        )

        assertEquals(4.5f, user.speedMps!!, 0.01f)
        assertEquals(100.0, user.altitudeMeters!!, 0.01)
        assertEquals(90f, user.bearingDegrees!!, 0.01f)
    }
}
