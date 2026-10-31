package com.example.gpstracker.core.places

import com.example.gpstracker.core.data.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun `formats meters under one km`() {
        val display = DistanceFormatter.format(250.0, DistanceUnit.KM)
        assertEquals("250", display.valueText)
        assertEquals(DistanceFormatter.DistanceDisplayUnit.M, display.unit)
    }

    @Test
    fun `formats kilometers`() {
        val display = DistanceFormatter.format(2_500.0, DistanceUnit.KM)
        assertEquals("2.5", display.valueText)
        assertEquals(DistanceFormatter.DistanceDisplayUnit.Km, display.unit)
    }

    @Test
    fun `formats feet under one mile`() {
        val display = DistanceFormatter.format(100.0, DistanceUnit.MI)
        assertEquals("328", display.valueText)
        assertEquals(DistanceFormatter.DistanceDisplayUnit.Ft, display.unit)
    }

    @Test
    fun `formats miles`() {
        val display = DistanceFormatter.format(3_218.0, DistanceUnit.MI)
        assertEquals("2", display.valueText)
        assertEquals(DistanceFormatter.DistanceDisplayUnit.Mi, display.unit)
    }
}
