package com.example.gpstracker.core.geo

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoResponseMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `photon keeps only returned address parts and osm id`() {
        val raw = """
            {"features":[{"geometry":{"coordinates":[73.037,33.729]},
            "properties":{"osm_type":"W","osm_id":42,"name":"Faisal Mosque","city":"Islamabad","country":"Pakistan"}}]}
        """.trimIndent()
        val parsed = json.decodeFromString(PhotonResponse.serializer(), raw)
        val suggestion = PhotonMapper.suggestions(parsed).single()
        assertEquals("way/42", suggestion.placeId)
        assertEquals("Faisal Mosque", suggestion.primaryText)
        assertTrue(suggestion.secondaryText.contains("Islamabad"))
        val details = PhotonMapper.details(parsed).single()
        assertEquals(33.729, details.latitude, 0.0)
        assertEquals(73.037, details.longitude, 0.0)
        val parts = PhotonMapper.addressParts(parsed.features.single().properties)
        assertNull(parts.thoroughfare)
        assertEquals("Islamabad", parts.locality)
        assertEquals("Pakistan", parts.countryName)
    }

    @Test
    fun `photon reverse uses the feature name when city is absent`() {
        val raw = """
            {"features":[{"properties":{"name":"Nai Abadi","state":"Punjab","country":"Pakistan"}}]}
        """.trimIndent()
        val parts = PhotonMapper.addressParts(
            json.decodeFromString(PhotonResponse.serializer(), raw).features.single().properties,
        )
        assertNull(parts.subLocality)
        assertEquals("Nai Abadi", parts.locality)
    }

    @Test
    fun `overpass uses node coordinates and way centers and skips unnamed`() {
        val raw = """
            {"elements":[
              {"type":"node","id":1,"lat":1.5,"lon":2.5,"tags":{"name":"City Hospital","amenity":"hospital","addr:city":"Lahore"}},
              {"type":"way","id":2,"center":{"lat":3.0,"lon":4.0},"tags":{"name":"Mall","shop":"mall"}},
              {"type":"node","id":3,"lat":9.0,"lon":9.0,"tags":{"amenity":"cafe"}}
            ]}
        """.trimIndent()
        val places = OverpassMapper.summaries(json.decodeFromString(OverpassResponse.serializer(), raw), maxResults = 20)
        assertEquals(2, places.size)
        assertEquals("node/1", places[0].placeId)
        assertEquals("Lahore", places[0].formattedAddress)
        assertEquals("way/2", places[1].placeId)
        assertEquals(3.0, places[1].latitude, 0.0)
        assertNull(places[1].formattedAddress)
    }

    @Test
    fun `mosque query includes religion tag`() {
        val query = overpassQuery("amenity", "place_of_worship", "religion", "muslim", 33.7, 73.0, 5000.0)
        assertTrue(query.contains("[\"religion\"=\"muslim\"]"))
        assertTrue(query.contains("around:5000,33.7,73.0"))
        assertTrue(query.contains("node") && query.contains("way") && query.contains("relation"))
    }
}
