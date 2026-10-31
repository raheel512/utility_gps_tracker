package com.example.gpstracker.core.data

/**
 * Appearance preference. Auto follows the system dark/light setting.
 */
enum class ThemeMode {
    Auto,
    Light,
    Dark,
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Auto
    }
}

/**
 * Distance / speed unit preference for gauges and odometer.
 */
enum class DistanceUnit {
    KM,
    MI,
    ;

    companion object {
        fun fromStorage(value: String?): DistanceUnit =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: KM
    }
}
