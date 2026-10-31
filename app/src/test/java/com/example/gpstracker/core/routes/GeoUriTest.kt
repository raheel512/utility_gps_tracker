package com.example.gpstracker.core.routes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoUriTest {
    @Test
    fun `pin uses coordinates and does not call google maps`() {
        val uri = GeoUri.pin(33.729, 73.037, "Faisal Mosque")
        assertTrue(uri.startsWith("geo:33.729000,73.037000"))
        assertTrue(uri.contains("Faisal"))
        assertTrue(!uri.contains("google"))
        assertTrue(!uri.contains("maps"))
    }

    @Test
    fun `blank label is coordinates only`() {
        assertEquals("geo:1.500000,2.250000", GeoUri.pin(1.5, 2.25, "  "))
    }
}
