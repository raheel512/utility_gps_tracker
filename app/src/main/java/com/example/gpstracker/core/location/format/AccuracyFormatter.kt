package com.example.gpstracker.core.location.format

import com.example.gpstracker.core.data.DistanceUnit
import kotlin.math.roundToInt

enum class AccuracyUnit { Meters, Feet }

data class AccuracyDisplay(val value: Int, val unit: AccuracyUnit)

/** Meters when Settings uses km, feet when it uses mi. Rendered through string resources in the UI. */
object AccuracyFormatter {

    private const val FEET_PER_METER = 3.28084

    fun toDisplay(accuracyMeters: Float, unit: DistanceUnit): AccuracyDisplay {
        val meters = accuracyMeters.coerceAtLeast(0f).toDouble()
        return when (unit) {
            DistanceUnit.KM -> AccuracyDisplay(meters.roundToInt(), AccuracyUnit.Meters)
            DistanceUnit.MI -> AccuracyDisplay((meters * FEET_PER_METER).roundToInt(), AccuracyUnit.Feet)
        }
    }
}
