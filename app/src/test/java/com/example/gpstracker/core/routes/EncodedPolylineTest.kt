package com.example.gpstracker.core.routes

import org.junit.Assert.assertEquals
import org.junit.Test

class EncodedPolylineTest {
    @Test
    fun `decodes Google documented example`() {
        // https://developers.google.com/maps/documentation/utilities/polylinealgorithm
        val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val points = EncodedPolyline.decode(encoded)
        assertEquals(3, points.size)
        assertEquals(38.5, points[0].latitude, 1e-5)
        assertEquals(-120.2, points[0].longitude, 1e-5)
        assertEquals(40.7, points[1].latitude, 1e-5)
        assertEquals(-120.95, points[1].longitude, 1e-5)
        assertEquals(43.252, points[2].latitude, 1e-5)
        assertEquals(-126.453, points[2].longitude, 1e-5)
        assertEquals(encoded, EncodedPolyline.encode(points))
    }

    @Test
    fun `precision 6 round trips before history stores precision 5`() {
        val original = listOf(
            com.example.gpstracker.core.places.LatLngPoint(33.729412, 73.037881),
            com.example.gpstracker.core.places.LatLngPoint(33.730102, 73.040225),
        )
        val precise = EncodedPolyline.decode(EncodedPolyline.encode(original, precision = 6), precision = 6)
        assertEquals(33.729412, precise[0].latitude, 1e-6)
        val stored = EncodedPolyline.decode(EncodedPolyline.encode(precise))
        assertEquals(2, stored.size)
        assertEquals(33.729412, stored[0].latitude, 1e-5)
    }
}
