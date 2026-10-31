package com.example.gpstracker.core.sensors

import org.junit.Assert.assertEquals
import org.junit.Test

class CardinalLabelTest {

    @Test
    fun `cardinal labels at rose points`() {
        assertEquals("N", cardinalLabel(0f))
        assertEquals("NNW", cardinalLabel(342f))
        assertEquals("E", cardinalLabel(90f))
        assertEquals("S", cardinalLabel(180f))
        assertEquals("W", cardinalLabel(270f))
    }
}
