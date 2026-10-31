package com.example.gpstracker.core.location.format

import com.example.gpstracker.core.data.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AccuracyFormatterTest {

    @Test
    fun `km setting shows meters`() {
        assertEquals(AccuracyDisplay(12, AccuracyUnit.Meters), AccuracyFormatter.toDisplay(12.3f, DistanceUnit.KM))
    }

    @Test
    fun `mi setting shows feet`() {
        // 10 m = 32.8084 ft
        assertEquals(AccuracyDisplay(33, AccuracyUnit.Feet), AccuracyFormatter.toDisplay(10f, DistanceUnit.MI))
        // 1 m = 3.28084 ft
        assertEquals(AccuracyDisplay(3, AccuracyUnit.Feet), AccuracyFormatter.toDisplay(1f, DistanceUnit.MI))
    }

    @Test
    fun `half meters round up`() {
        assertEquals(AccuracyDisplay(13, AccuracyUnit.Meters), AccuracyFormatter.toDisplay(12.5f, DistanceUnit.KM))
    }

    @Test
    fun `zero and negative accuracy clamp to zero`() {
        assertEquals(AccuracyDisplay(0, AccuracyUnit.Meters), AccuracyFormatter.toDisplay(0f, DistanceUnit.KM))
        assertEquals(AccuracyDisplay(0, AccuracyUnit.Feet), AccuracyFormatter.toDisplay(-4f, DistanceUnit.MI))
    }
}
