package com.example.gpstracker.core.routes

/**
 * Formats planned duration for UI. Minutes are rounded to the nearest whole minute.
 */
object DurationFormatter {
    data class Display(
        val totalMinutes: Int,
        val hours: Int,
        val remainingMinutes: Int,
    )

    fun display(durationSeconds: Long): Display {
        val minutes = ((durationSeconds + 30) / 60).toInt().coerceAtLeast(1)
        return Display(
            totalMinutes = minutes,
            hours = minutes / 60,
            remainingMinutes = minutes % 60,
        )
    }
}
