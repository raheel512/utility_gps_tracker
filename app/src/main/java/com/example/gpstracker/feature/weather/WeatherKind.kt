package com.example.gpstracker.feature.weather

import androidx.annotation.StringRes
import com.example.gpstracker.R

/**
 * WMO weather code from Open-Meteo, mapped to the two skies in Figma
 * (55505:3965 fair, 55505:4088 rain) and a condition label.
 */
enum class WeatherKind(
    @StringRes val labelRes: Int,
    val rainSky: Boolean,
) {
    Clear(R.string.weather_clear, rainSky = false),
    MainlyClear(R.string.weather_mainly_clear, rainSky = false),
    ScatteredClouds(R.string.weather_scattered, rainSky = false),
    Overcast(R.string.weather_overcast, rainSky = false),
    Fog(R.string.weather_fog, rainSky = false),
    Drizzle(R.string.weather_drizzle, rainSky = true),
    Rain(R.string.weather_rain, rainSky = true),
    HighRain(R.string.weather_high_rain, rainSky = true),
    FreezingRain(R.string.weather_freezing_rain, rainSky = true),
    Snow(R.string.weather_snow, rainSky = true),
    Showers(R.string.weather_showers, rainSky = true),
    Thunder(R.string.weather_thunder, rainSky = true),
    ;

    companion object {
        fun fromCode(code: Int): WeatherKind = when (code) {
            0 -> Clear
            1 -> MainlyClear
            2 -> ScatteredClouds
            3 -> Overcast
            45, 48 -> Fog
            51, 53, 55, 56 -> Drizzle
            61, 63, 80 -> Rain
            65, 82 -> HighRain
            57, 66, 67 -> FreezingRain
            71, 73, 75, 77, 85, 86 -> Snow
            81 -> Showers
            95, 96, 99 -> Thunder
            else -> Overcast
        }
    }
}
