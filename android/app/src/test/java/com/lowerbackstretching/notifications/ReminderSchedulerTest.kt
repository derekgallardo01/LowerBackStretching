package com.lowerbackstretching.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderSchedulerTest {

    private fun now(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }
    }

    @Test
    fun returns_today_when_time_is_still_ahead() {
        val now = now(2026, 5, 15, 6, 0) // 06:00 UTC
        val next = nextOccurrence(8, 0, now)
        val expected = now(2026, 5, 15, 8, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun returns_tomorrow_when_time_has_already_passed() {
        val now = now(2026, 5, 15, 9, 0) // past 8:00
        val next = nextOccurrence(8, 0, now)
        val expected = now(2026, 5, 16, 8, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun returns_tomorrow_when_time_is_exactly_now() {
        val now = now(2026, 5, 15, 8, 0)
        val next = nextOccurrence(8, 0, now)
        val expected = now(2026, 5, 16, 8, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun handles_minute_precision() {
        val now = now(2026, 5, 15, 8, 30) // past 8:15
        val next = nextOccurrence(8, 15, now)
        val expected = now(2026, 5, 16, 8, 15).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun handles_midnight() {
        val now = now(2026, 5, 15, 23, 30)
        val next = nextOccurrence(0, 0, now)
        val expected = now(2026, 5, 16, 0, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun handles_end_of_month_rollover() {
        val now = now(2026, 5, 31, 23, 30)
        val next = nextOccurrence(8, 0, now)
        val expected = now(2026, 6, 1, 8, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun handles_end_of_year_rollover() {
        val now = now(2026, 12, 31, 23, 30)
        val next = nextOccurrence(8, 0, now)
        val expected = now(2027, 1, 1, 8, 0).timeInMillis
        assertThat(next).isEqualTo(expected)
    }

    @Test
    fun returns_a_value_in_the_future() {
        val now = now(2026, 5, 15, 12, 0)
        val next = nextOccurrence(8, 0, now)
        assertThat(next).isGreaterThan(now.timeInMillis)
    }
}
