package com.example.gpstracker.feature.compass

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassFormatTest {

    @Test
    fun `design sample reads as degrees minutes seconds`() {
        val latitude = 40.0 + 5.0 / 60.0 + 35.0 / 3600.0
        val longitude = 120.0 + 2.0 / 60.0 + 1.0 / 3600.0
        assertEquals("40°5'35\"", formatDms(latitude))
        assertEquals("120°2'1\"", formatDms(longitude))
        assertEquals(CompassHemisphere.North, latitudeHemisphere(latitude))
        assertEquals(CompassHemisphere.East, longitudeHemisphere(longitude))
    }

    @Test
    fun `negative coordinates use south and west without a minus`() {
        assertEquals("33°52'8\"", formatDms(-33.8688))
        assertEquals(CompassHemisphere.South, latitudeHemisphere(-33.8688))
        assertEquals(CompassHemisphere.West, longitudeHemisphere(-151.2093))
    }

    @Test
    fun `zero and values that round to zero seconds stay north and east`() {
        assertEquals("0°0'0\"", formatDms(0.0))
        assertEquals(CompassHemisphere.North, latitudeHemisphere(0.0))
        assertEquals(CompassHemisphere.East, longitudeHemisphere(0.0))
        assertEquals(CompassHemisphere.North, latitudeHemisphere(-0.00001))
        assertEquals(CompassHemisphere.East, longitudeHemisphere(-0.00001))
    }

    @Test
    fun `sixty seconds rolls into the next minute`() {
        val value = 10.0 + 59.6 / 3600.0
        assertEquals("10°1'0\"", formatDms(value))
    }

    @Test
    fun `heading has no space and 360 displays as 0`() {
        assertEquals("0°N", formatHeading(0f, "N"))
        assertEquals("0°N", formatHeading(359.6f, "N"))
        assertEquals("23°NNE", formatHeading(23.2f, "NNE"))
    }

    @Test
    fun `altitude is integer meters with no space`() {
        assertEquals("991m", formatAltitudeMeters(991.4))
        assertEquals("991m", formatAltitudeMeters(990.5))
    }
}
