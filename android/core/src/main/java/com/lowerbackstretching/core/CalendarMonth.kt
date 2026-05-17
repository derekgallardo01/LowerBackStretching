package com.lowerbackstretching.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Date math for rendering a month grid. Returns weekday symbols and a
 * week-by-week list of dates that cover the month plus the leading/trailing
 * neighbour days needed to fill the first and last weeks. Pure — pulled
 * out of `CalendarScreen` so the view stays focused on layout.
 */
data class CalendarMonth(
    val month: YearMonth,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
) {
    /** 4–6 rows of 7 dates each. Cells outside `month` are still real dates. */
    val weeks: List<List<LocalDate>> by lazy {
        val firstOfMonth = month.atDay(1)
        val offset = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
        val totalCells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
        val start = firstOfMonth.minusDays(offset.toLong())
        (0 until totalCells)
            .map { start.plusDays(it.toLong()) }
            .chunked(7)
    }

    fun weekdayLabels(locale: Locale = Locale.getDefault()): List<String> {
        val rotated = DayOfWeek.values().toList().rotated(firstDayOfWeek.value - 1)
        return rotated.map { it.getDisplayName(TextStyle.NARROW, locale) }
    }

    fun isInMonth(date: LocalDate): Boolean =
        date.month == month.month && date.year == month.year
}

private fun <T> List<T>.rotated(by: Int): List<T> {
    if (isEmpty()) return this
    val k = ((by % size) + size) % size
    return drop(k) + take(k)
}
