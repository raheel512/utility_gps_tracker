package com.example.gpstracker.feature.camera.stamp

import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.location.format.CoordinateFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object StampFormatter {

    /** Matches Figma: "14 Oct 2024 · 10:24:18 AM" */
    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy · h:mm:ss a", Locale.ENGLISH)

    fun format(snapshot: StampSnapshot): StampRenderModel {
        val coords = if (snapshot.latitude != null && snapshot.longitude != null) {
            CoordinateFormatter.format(snapshot.latitude, snapshot.longitude)
        } else {
            null
        }
        return StampRenderModel(
            dateTimeLine = formatDateTime(snapshot.dateTime),
            gmtBadge = formatGmtBadge(snapshot.zoneId, snapshot.dateTime),
            addressLine = snapshot.addressLine?.trim()?.takeIf { it.isNotEmpty() },
            coordinatesLine = coords,
            altitudeLine = snapshot.altitudeMeters?.let { formatAltitude(it) },
            speedLine = snapshot.speedMps?.let { formatSpeed(it, snapshot.distanceUnit) },
            pressureLine = snapshot.pressureHpa?.let { formatPressure(it) },
            headingLine = formatHeading(snapshot.headingDegrees, snapshot.headingCardinal),
        )
    }

    fun formatDateTime(dateTime: ZonedDateTime): String = dateTime.format(dateTimeFormatter)

    /**
     * "GMT+5" / "GMT-4" / "GMT+5:30". Uses the offset at [dateTime], not a fixed zone name.
     */
    fun formatGmtBadge(zoneId: ZoneId, dateTime: ZonedDateTime = ZonedDateTime.now(zoneId)): String {
        val offset = dateTime.withZoneSameInstant(zoneId).offset
        val totalSeconds = offset.totalSeconds
        val sign = if (totalSeconds >= 0) "+" else "-"
        val abs = kotlin.math.abs(totalSeconds)
        val hours = abs / 3600
        val minutes = (abs % 3600) / 60
        return if (minutes == 0) "GMT$sign$hours" else "GMT$sign$hours:${"%02d".format(minutes)}"
    }

    fun formatAltitude(meters: Double): String =
        "${meters.roundToInt()} m"

    fun formatSpeed(speedMps: Float, unit: DistanceUnit): String {
        val value = when (unit) {
            DistanceUnit.KM -> speedMps * 3.6f
            DistanceUnit.MI -> speedMps * 2.2369363f
        }.roundToInt()
        val suffix = when (unit) {
            DistanceUnit.KM -> "km/h"
            DistanceUnit.MI -> "mph"
        }
        return "$value $suffix"
    }

    fun formatPressure(hpa: Float): String = "${hpa.roundToInt()} hPa"

    fun formatHeading(degrees: Float?, cardinal: String?): String? {
        if (degrees == null || cardinal == null) return null
        return "${degrees.roundToInt()}° $cardinal"
    }

    /** Design "HDOP" substitute — horizontal accuracy. */
    fun formatAccuracyMeters(accuracyMeters: Float): String =
        "± ${accuracyMeters.roundToInt()} m"
}
