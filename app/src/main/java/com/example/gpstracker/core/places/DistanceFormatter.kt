package com.example.gpstracker.core.places

import com.example.gpstracker.core.data.DistanceUnit
import kotlin.math.roundToInt

/**
 * Formats a distance in meters for list UI using the Settings km/mi preference.
 */
object DistanceFormatter {
    private const val METERS_PER_KM = 1_000.0
    private const val METERS_PER_MILE = 1_609.344
    private const val FEET_PER_METER = 3.28084

    data class DistanceDisplay(
        val valueText: String,
        val unit: DistanceDisplayUnit,
    )

    enum class DistanceDisplayUnit { Km, M, Mi, Ft }

    fun format(meters: Double, unit: DistanceUnit): DistanceDisplay {
        val m = meters.coerceAtLeast(0.0)
        return when (unit) {
            DistanceUnit.KM -> {
                if (m >= METERS_PER_KM) {
                    DistanceDisplay(formatOneDecimal(m / METERS_PER_KM), DistanceDisplayUnit.Km)
                } else {
                    DistanceDisplay(m.roundToInt().toString(), DistanceDisplayUnit.M)
                }
            }
            DistanceUnit.MI -> {
                if (m >= METERS_PER_MILE) {
                    DistanceDisplay(formatOneDecimal(m / METERS_PER_MILE), DistanceDisplayUnit.Mi)
                } else {
                    DistanceDisplay((m * FEET_PER_METER).roundToInt().toString(), DistanceDisplayUnit.Ft)
                }
            }
        }
    }

    private fun formatOneDecimal(value: Double): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}
