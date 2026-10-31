package com.example.gpstracker.feature.history

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

enum class HistoryRange {
    All,
    Today,
    Yesterday,
    ThisWeek,
    ThisMonth,
}

object HistoryDateFilter {
    fun startedAtInRange(
        startedAtMillis: Long,
        range: HistoryRange,
        clock: Clock,
        locale: Locale = Locale.getDefault(),
    ): Boolean {
        if (range == HistoryRange.All) return true
        val zone = clock.zone
        val now = ZonedDateTime.now(clock)
        val zdt = Instant.ofEpochMilli(startedAtMillis).atZone(zone)
        val todayStart = now.toLocalDate().atStartOfDay(zone)
        return when (range) {
            HistoryRange.All -> true
            HistoryRange.Today -> zdt >= todayStart && zdt < todayStart.plusDays(1)
            HistoryRange.Yesterday -> {
                val yesterday = todayStart.minusDays(1)
                zdt >= yesterday && zdt < todayStart
            }
            HistoryRange.ThisWeek -> {
                val weekFields = WeekFields.of(locale)
                val weekStart = now.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(weekFields.firstDayOfWeek))
                    .atStartOfDay(zone)
                zdt >= weekStart && zdt < weekStart.plusWeeks(1)
            }
            HistoryRange.ThisMonth -> {
                val monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
                zdt >= monthStart && zdt < monthStart.plusMonths(1)
            }
        }
    }
}
