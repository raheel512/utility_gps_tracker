package com.example.gpstracker.feature.compass

import kotlin.math.abs
import kotlin.math.roundToInt

/** Two-letter hemisphere codes from the compass design: NL, SL, EL, WL. */
enum class CompassHemisphere {
    North,
    South,
    East,
    West,
}

/**
 * Degrees-minutes-seconds without zero padding, e.g. 40°5'35".
 * Seconds are rounded, and a value of 60 rolls into the next minute.
 */
fun formatDms(value: Double): String {
    var seconds = (abs(value) * 3600.0).roundToInt()
    val degrees = seconds / 3600
    seconds %= 3600
    val minutes = seconds / 60
    val secs = seconds % 60
    return "$degrees°$minutes'$secs\""
}

fun latitudeHemisphere(latitude: Double): CompassHemisphere {
    val seconds = (abs(latitude) * 3600.0).roundToInt()
    return if (latitude < 0.0 && seconds != 0) CompassHemisphere.South else CompassHemisphere.North
}

fun longitudeHemisphere(longitude: Double): CompassHemisphere {
    val seconds = (abs(longitude) * 3600.0).roundToInt()
    return if (longitude < 0.0 && seconds != 0) CompassHemisphere.West else CompassHemisphere.East
}

/** "23°NNE". 360° displays as 0°. */
fun formatHeading(degrees: Float, cardinal: String): String {
    val rounded = ((degrees.roundToInt() % 360) + 360) % 360
    return "$rounded°$cardinal"
}

/** Integer meters with no space, e.g. 991m. */
fun formatAltitudeMeters(meters: Double): String = "${meters.roundToInt()}m"
