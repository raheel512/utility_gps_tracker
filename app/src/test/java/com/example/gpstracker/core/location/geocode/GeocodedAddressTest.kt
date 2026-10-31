package com.example.gpstracker.core.location.geocode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeocodedAddressTest {

    @Test
    fun `joins all parts in design order`() {
        val address = GeocodedAddress("9th Ave", "F-9", "Islamabad", "Pakistan")
        assertEquals("9th Ave, F-9, Islamabad, Pakistan", address.displayLine())
    }

    @Test
    fun `skips missing and blank parts`() {
        val address = GeocodedAddress(thoroughfare = null, subLocality = " ", locality = "Islamabad", countryName = "Pakistan")
        assertEquals("Islamabad, Pakistan", address.displayLine())
    }

    @Test
    fun `drops duplicate parts`() {
        val address = GeocodedAddress(locality = "Singapore", countryName = "Singapore")
        assertEquals("Singapore", address.displayLine())
    }

    @Test
    fun `empty address has no line`() {
        assertNull(GeocodedAddress().displayLine())
    }

    @Test
    fun `place label prefers the neighborhood then the city`() {
        val address = GeocodedAddress(subLocality = "Nai Abadi", locality = "Islamabad", countryName = "Pakistan")
        assertEquals("Nai Abadi", address.placeLabel())
        assertEquals("Islamabad", GeocodedAddress(locality = "Islamabad", countryName = "Pakistan").placeLabel())
        assertNull(GeocodedAddress().placeLabel())
    }
}
