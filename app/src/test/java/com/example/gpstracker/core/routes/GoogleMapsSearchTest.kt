package com.example.gpstracker.core.routes

import com.example.gpstracker.core.places.NearbyCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsSearchTest {

    @Test
    fun `nearby search asks Google Maps for that category near the user`() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=Hospital+near+me",
            GoogleMapsSearch.nearbyUri("Hospital"),
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=famous+places+near+me",
            GoogleMapsSearch.nearbyUri("famous places"),
        )
    }

    @Test
    fun `cafe tile searches for cafes rather than the brand label`() {
        assertEquals("cafe", NearbyCategory.CcdCafe.mapsSearchQuery("CCD / Cafe"))
        assertEquals("movie theater", NearbyCategory.Theater.mapsSearchQuery("Theater"))
        assertEquals("Bank", NearbyCategory.Bank.mapsSearchQuery("Bank"))
    }
}
