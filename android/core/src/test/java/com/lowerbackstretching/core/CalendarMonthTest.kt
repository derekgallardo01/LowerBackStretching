package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarMonthTest {

    @Test
    fun weeks_cover_full_month_and_are_35_or_42_cells() {
        val m = CalendarMonth(YearMonth.of(2026, 5))
        val cells = m.weeks.flatten()
        assertThat(cells.size).isAnyOf(35, 42)
        assertThat(cells.count { m.isInMonth(it) }).isEqualTo(31)
    }

    @Test
    fun weekday_labels_has_seven_entries() {
        // English narrow weekday symbols collide (M T W T F S S) — so labels
        // are NOT all unique. Just verify the count.
        val labels = CalendarMonth(YearMonth.of(2026, 5)).weekdayLabels()
        assertThat(labels).hasSize(7)
    }

    @Test
    fun isInMonth_handles_boundary_days() {
        val m = CalendarMonth(YearMonth.of(2026, 5))
        assertThat(m.isInMonth(LocalDate.of(2026, 5, 1))).isTrue()
        assertThat(m.isInMonth(LocalDate.of(2026, 5, 31))).isTrue()
        assertThat(m.isInMonth(LocalDate.of(2026, 4, 30))).isFalse()
        assertThat(m.isInMonth(LocalDate.of(2026, 6, 1))).isFalse()
    }

    @Test
    fun february_leap_year_has_29_days() {
        val m = CalendarMonth(YearMonth.of(2024, 2))
        assertThat(m.weeks.flatten().count { m.isInMonth(it) }).isEqualTo(29)
    }

    @Test
    fun february_non_leap_year_has_28_days() {
        val m = CalendarMonth(YearMonth.of(2025, 2))
        assertThat(m.weeks.flatten().count { m.isInMonth(it) }).isEqualTo(28)
    }

    @Test
    fun sunday_first_day_of_week_changes_first_offset() {
        val mon = CalendarMonth(YearMonth.of(2026, 5), firstDayOfWeek = DayOfWeek.MONDAY)
        val sun = CalendarMonth(YearMonth.of(2026, 5), firstDayOfWeek = DayOfWeek.SUNDAY)
        // The first date in each grid is at the corresponding firstDayOfWeek.
        assertThat(mon.weeks.first().first().dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(sun.weeks.first().first().dayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
    }
}
