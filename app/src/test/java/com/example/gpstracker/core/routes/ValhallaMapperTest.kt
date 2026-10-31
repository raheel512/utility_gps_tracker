package com.example.gpstracker.core.routes

import com.example.gpstracker.core.geo.ValhallaMapper
import com.example.gpstracker.core.places.LatLngPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValhallaMapperTest {
    private val json = Json { encodeDefaults = true; explicitNulls = false }

    @Test
    fun `drive request uses coordinates and avoid flags`() {
        val request = ValhallaMapper.request(
            origin = LatLngPoint(33.7, 73.0),
            destination = LatLngPoint(33.73, 73.04),
            via = null,
            avoidTolls = true,
            avoidFerries = true,
        )
        assertEquals("auto", request.costing)
        assertEquals(2, request.locations.size)
        assertEquals(33.7, request.locations[0].lat, 0.0)
        assertEquals(73.04, request.locations[1].lon, 0.0)
        assertEquals(0.0, request.costing_options!!.auto.use_tolls, 0.0)
        assertEquals(0.0, request.costing_options.auto.use_ferry, 0.0)
        val encoded = json.encodeToString(request)
        assertEquals(false, encoded.contains("placeId"))
        assertEquals(false, encoded.contains("googleapis"))
    }

    @Test
    fun `via is a through location between origin and destination`() {
        val request = ValhallaMapper.request(
            origin = LatLngPoint(1.0, 2.0),
            destination = LatLngPoint(5.0, 6.0),
            via = LatLngPoint(3.0, 4.0),
            avoidTolls = false,
            avoidFerries = false,
        )
        assertEquals(3, request.locations.size)
        assertEquals("through", request.locations[1].type)
        assertNull(request.costing_options)
    }
}
