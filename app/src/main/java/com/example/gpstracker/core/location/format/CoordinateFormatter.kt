package com.example.gpstracker.core.location.format

import java.util.Locale
import kotlin.math.abs

/**
 * Decimal degrees with hemisphere letters, 4 decimals: "33.6938° N, 73.0489° E".
 * Uses [Locale.ROOT] so digits and the decimal point never change with the app language.
 */
object CoordinateFormatter {

    fun format(latitude: Double, longitude: Double): String =
        "${formatLatitude(latitude)}, ${formatLongitude(longitude)}"

    fun formatLatitude(latitude: Double): String = formatAxis(latitude, positive = 'N', negative = 'S')

    fun formatLongitude(longitude: Double): String = formatAxis(longitude, positive = 'E', negative = 'W')

    private fun formatAxis(value: Double, positive: Char, negative: Char): String {
        val magnitude = String.format(Locale.ROOT, "%.4f", abs(value))
        // A value that rounds to zero has no hemisphere; use the positive letter instead of "S"/"W".
        val hemisphere = if (value < 0 && magnitude.any { it in '1'..'9' }) negative else positive
        return "$magnitude° $hemisphere"
    }
}
