package com.example.gpstracker.feature.camera.stamp

import com.example.gpstracker.core.data.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class StampFormatterTest {

    private val zone = ZoneId.of("Asia/Karachi") // UTC+5, no DST

    @Test
    fun `date time matches design pattern`() {
        val dt = ZonedDateTime.of(2024, 10, 14, 10, 24, 18, 0, zone)
        assertEquals("14 Oct 2024 · 10:24:18 AM", StampFormatter.formatDateTime(dt))
    }

    @Test
    fun `gmt badge uses offset hours`() {
        val dt = ZonedDateTime.of(2024, 10, 14, 10, 24, 18, 0, zone)
        assertEquals("GMT+5", StampFormatter.formatGmtBadge(zone, dt))
    }

    @Test
    fun `gmt badge with half-hour offset`() {
        val kolkata = ZoneId.of("Asia/Kolkata")
        val dt = ZonedDateTime.of(2024, 10, 14, 10, 0, 0, 0, kolkata)
        assertEquals("GMT+5:30", StampFormatter.formatGmtBadge(kolkata, dt))
    }

    @Test
    fun `speed converts mps to kmh and mph`() {
        // 10 m/s * 3.6 = 36 km/h
        assertEquals("36 km/h", StampFormatter.formatSpeed(10f, DistanceUnit.KM))
        // 10 m/s * 2.2369363 ≈ 22 mph
        assertEquals("22 mph", StampFormatter.formatSpeed(10f, DistanceUnit.MI))
    }

    @Test
    fun `omits unavailable fields`() {
        val model = StampFormatter.format(
            StampSnapshot(
                dateTime = ZonedDateTime.of(2024, 10, 14, 10, 24, 18, 0, zone),
                zoneId = zone,
            ),
        )
        assertNull(model.addressLine)
        assertNull(model.coordinatesLine)
        assertNull(model.altitudeLine)
        assertNull(model.speedLine)
        assertNull(model.pressureLine)
        assertNull(model.headingLine)
        assertEquals("GMT+5", model.gmtBadge)
    }

    @Test
    fun `formats full stamp when all fields present`() {
        val model = StampFormatter.format(
            StampSnapshot(
                dateTime = ZonedDateTime.of(2024, 10, 14, 10, 24, 18, 0, zone),
                zoneId = zone,
                addressLine = "Kashmir Highway, Sector G-9, Islamabad",
                latitude = 33.6938,
                longitude = 73.0489,
                altitudeMeters = 518.4,
                speedMps = 0f,
                distanceUnit = DistanceUnit.KM,
                pressureHpa = 1012.2f,
                headingDegrees = 342.4f,
                headingCardinal = "NNW",
            ),
        )
        assertEquals("14 Oct 2024 · 10:24:18 AM", model.dateTimeLine)
        assertEquals("Kashmir Highway, Sector G-9, Islamabad", model.addressLine)
        assertEquals("33.6938° N, 73.0489° E", model.coordinatesLine)
        assertEquals("518 m", model.altitudeLine)
        assertEquals("0 km/h", model.speedLine)
        assertEquals("1012 hPa", model.pressureLine)
        assertEquals("342° NNW", model.headingLine)
    }

    @Test
    fun `accuracy substitutes for HDOP`() {
        assertEquals("± 5 m", StampFormatter.formatAccuracyMeters(5.4f))
    }

    @Test
    fun `blank address is omitted`() {
        val model = StampFormatter.format(
            StampSnapshot(
                dateTime = ZonedDateTime.of(2024, 10, 14, 10, 24, 18, 0, zone),
                zoneId = zone,
                addressLine = "  ",
            ),
        )
        assertNull(model.addressLine)
    }
}
