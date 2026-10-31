package com.example.gpstracker.feature.weather

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.roundToInt

object WeatherFormat {
    private val observedParsers = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
    )
    private val clock = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun temperature(celsius: Double): String = "${celsius.roundToInt()}°"

    fun windMetersPerSecond(mps: Double): String = String.format(Locale.US, "%.1f", mps)

    fun pressureHpa(hpa: Double): String = String.format(Locale.US, "%.1f", hpa)

    fun percent(value: Double): Int = value.roundToInt()

    /** Local clock such as "3:33 PM". Null when the service time cannot be read. */
    fun clockLabel(isoLocal: String): String? {
        val parsed = observedParsers.firstNotNullOfOrNull { formatter ->
            try {
                LocalDateTime.parse(isoLocal, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        } ?: return null
        return clock.format(parsed).replace('\u202f', ' ').replace('\u00a0', ' ')
    }
}
