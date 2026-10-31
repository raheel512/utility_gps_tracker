package com.example.gpstracker.core.places

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlacesMemoryCacheTest {

    @Test
    fun `cache hit returns stored value`() {
        val cache = PlacesMemoryCache(nowMs = { 1_000L })
        cache.put("k", listOf("a"))
        assertEquals(listOf("a"), cache.get<List<String>>("k"))
    }

    @Test
    fun `cache miss after ttl`() {
        var now = 1_000L
        val cache = PlacesMemoryCache(ttlMs = 100, nowMs = { now })
        cache.put("k", "value")
        now = 1_200L
        assertNull(cache.get<String>("k"))
    }

    @Test
    fun `location key rounds to about 100 m`() {
        assertEquals(
            PlacesMemoryCache.locationKey(33.6001, 73.0001),
            PlacesMemoryCache.locationKey(33.6004, 73.0004),
        )
        assertEquals(
            false,
            PlacesMemoryCache.locationKey(33.600, 73.000) ==
                PlacesMemoryCache.locationKey(33.602, 73.002),
        )
    }
}
