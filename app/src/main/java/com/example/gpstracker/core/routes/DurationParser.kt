package com.example.gpstracker.core.routes

/**
 * Parses a protobuf Duration string such as `"312s"` used by Compute Routes.
 */
object DurationParser {
    fun secondsOrZero(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        val trimmed = value.trim().removeSuffix("s").removeSuffix("S")
        return trimmed.toDoubleOrNull()?.toLong() ?: 0L
    }
}
