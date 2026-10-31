package com.example.gpstracker.feature.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class HistoryDateFilterTest {
    private val zone = ZoneId.of("Asia/Karachi")
    // Thursday 24 Oct 2024 12:00 PKT
    private val clock: Clock = Clock.fixed(Instant.parse("2024-10-24T07:00:00Z"), zone)
    private val locale = Locale.US

    @Test
    fun `today includes midnight and excludes next midnight`() {
        val start = Instant.parse("2024-10-23T19:00:00Z").toEpochMilli() // 00:00 PKT
        val justBeforeNext = Instant.parse("2024-10-24T18:59:59Z").toEpochMilli()
        val nextMidnight = Instant.parse("2024-10-24T19:00:00Z").toEpochMilli()
        assertTrue(HistoryDateFilter.startedAtInRange(start, HistoryRange.Today, clock, locale))
        assertTrue(HistoryDateFilter.startedAtInRange(justBeforeNext, HistoryRange.Today, clock, locale))
        assertFalse(HistoryDateFilter.startedAtInRange(nextMidnight, HistoryRange.Today, clock, locale))
    }

    @Test
    fun `yesterday is previous local day`() {
        val yesterdayStart = Instant.parse("2024-10-22T19:00:00Z").toEpochMilli()
        val todayStart = Instant.parse("2024-10-23T19:00:00Z").toEpochMilli()
        assertTrue(HistoryDateFilter.startedAtInRange(yesterdayStart, HistoryRange.Yesterday, clock, locale))
        assertFalse(HistoryDateFilter.startedAtInRange(todayStart, HistoryRange.Yesterday, clock, locale))
    }

    @Test
    fun `this week uses locale first day of week`() {
        // US week starts Sunday 20 Oct 2024 00:00 PKT = 19 Oct 19:00Z
        val weekStart = Instant.parse("2024-10-19T19:00:00Z").toEpochMilli()
        val beforeWeek = Instant.parse("2024-10-19T18:59:59Z").toEpochMilli()
        assertTrue(HistoryDateFilter.startedAtInRange(weekStart, HistoryRange.ThisWeek, clock, locale))
        assertFalse(HistoryDateFilter.startedAtInRange(beforeWeek, HistoryRange.ThisWeek, clock, locale))
    }

    @Test
    fun `this month starts on the first`() {
        val monthStart = Instant.parse("2024-09-30T19:00:00Z").toEpochMilli() // 1 Oct 00:00 PKT
        val before = Instant.parse("2024-09-30T18:59:59Z").toEpochMilli()
        assertTrue(HistoryDateFilter.startedAtInRange(monthStart, HistoryRange.ThisMonth, clock, locale))
        assertFalse(HistoryDateFilter.startedAtInRange(before, HistoryRange.ThisMonth, clock, locale))
    }

    @Test
    fun `all includes every timestamp`() {
        assertTrue(HistoryDateFilter.startedAtInRange(0L, HistoryRange.All, clock, locale))
    }
}
