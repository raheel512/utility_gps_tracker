package com.example.gpstracker.feature.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherFormatTest {
    @Test
    fun `temperature rounds to the nearest degree`() {
        assertEquals("29°", WeatherFormat.temperature(29.4))
        assertEquals("30°", WeatherFormat.temperature(29.6))
    }

    @Test
    fun `wind and pressure keep one decimal`() {
        assertEquals("2.9", WeatherFormat.windMetersPerSecond(2.94))
        assertEquals("1007.0", WeatherFormat.pressureHpa(1007.04))
    }

    @Test
    fun `clock labels read the service local time`() {
        assertEquals("3:33 PM", WeatherFormat.clockLabel("2026-10-31T15:33"))
        assertNull(WeatherFormat.clockLabel("not-a-time"))
    }
}
