package com.example.gpstracker.core.routes

import com.example.gpstracker.core.places.LatLngPoint
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Encoded polyline. Precision 5 matches polylines already stored in history.
 * Valhalla shapes use precision 6 and are re-encoded to 5 before they are saved.
 */
object EncodedPolyline {
    fun decode(encoded: String, precision: Int = 5): List<LatLngPoint> {
        if (encoded.isEmpty()) return emptyList()
        val factor = 10.0.pow(precision)
        val points = ArrayList<LatLngPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            val dlat = nextDelta(encoded, index).also { index = it.index }
            lat += dlat.value
            if (index >= len) break
            val dlng = nextDelta(encoded, index).also { index = it.index }
            lng += dlng.value
            points.add(LatLngPoint(latitude = lat / factor, longitude = lng / factor))
        }
        return points
    }

    fun encode(points: List<LatLngPoint>, precision: Int = 5): String {
        val factor = 10.0.pow(precision)
        var lastLat = 0
        var lastLng = 0
        val out = StringBuilder()
        points.forEach { point ->
            val lat = (point.latitude * factor).roundToInt()
            val lng = (point.longitude * factor).roundToInt()
            writeSigned(lat - lastLat, out)
            writeSigned(lng - lastLng, out)
            lastLat = lat
            lastLng = lng
        }
        return out.toString()
    }

    private fun writeSigned(value: Int, out: StringBuilder) {
        var signed = if (value < 0) (value shl 1).inv() else value shl 1
        while (signed >= 0x20) {
            out.append(((0x20 or (signed and 0x1f)) + 63).toChar())
            signed = signed shr 5
        }
        out.append((signed + 63).toChar())
    }

    private data class Delta(val value: Int, val index: Int)

    private fun nextDelta(encoded: String, start: Int): Delta {
        var result = 0
        var shift = 0
        var index = start
        var b: Int
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20 && index < encoded.length)
        val value = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        return Delta(value, index)
    }
}
