package com.example.gpstracker.core.places

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NearbyCategoryMappingTest {

    @Test
    fun `see more order has 21 categories`() {
        assertEquals(21, NearbyCategory.seeMoreOrder.size)
    }

    @Test
    fun `maps categories to openstreetmap tags`() {
        assertEquals("tourism", NearbyCategory.Hotels.osmKey)
        assertEquals("hotel", NearbyCategory.Hotels.osmValue)
        assertEquals("amenity", NearbyCategory.CcdCafe.osmKey)
        assertEquals("cafe", NearbyCategory.CcdCafe.osmValue)
        assertEquals("amenity", NearbyCategory.Hospital.osmKey)
        assertEquals("hospital", NearbyCategory.Hospital.osmValue)
        assertEquals("religion", NearbyCategory.Mosques.osmExtraKey)
        assertEquals("muslim", NearbyCategory.Mosques.osmExtraValue)
        assertEquals("shop", NearbyCategory.Garage.osmKey)
        assertEquals("car_repair", NearbyCategory.Garage.osmValue)
    }

    @Test
    fun `fromId and osm value lookup`() {
        assertEquals(NearbyCategory.Garage, NearbyCategory.fromId("garage"))
        assertEquals("car_repair", osmValueForCategoryId("garage"))
        assertNull(NearbyCategory.fromId("unknown"))
        assertNull(osmValueForCategoryId("unknown"))
    }
}
