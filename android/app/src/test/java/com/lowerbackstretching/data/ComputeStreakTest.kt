package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class ComputeStreakTest {

    private val today = LocalDate.of(2026, 5, 15)

    @Test
    fun `empty set returns zero`() {
        assertThat(computeStreak(emptySet(), today)).isEqualTo(0)
    }

    @Test
    fun `only today counts as 1`() {
        assertThat(computeStreak(setOf(today), today)).isEqualTo(1)
    }

    @Test
    fun `three consecutive days ending today is 3`() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2))
        assertThat(computeStreak(days, today)).isEqualTo(3)
    }

    @Test
    fun `today missing but yesterday present uses grace day`() {
        val yesterday = today.minusDays(1)
        val days = setOf(yesterday, yesterday.minusDays(1))
        assertThat(computeStreak(days, today)).isEqualTo(2)
    }

    @Test
    fun `today and day before yesterday but missing yesterday breaks streak at 1`() {
        val days = setOf(today, today.minusDays(2))
        assertThat(computeStreak(days, today)).isEqualTo(1)
    }

    @Test
    fun `entirely-old days return zero (gap larger than grace)`() {
        val old = today.minusDays(5)
        assertThat(computeStreak(setOf(old, old.minusDays(1)), today)).isEqualTo(0)
    }

    @Test
    fun `ten consecutive days returns 10`() {
        val days = (0L..9L).map { today.minusDays(it) }.toSet()
        assertThat(computeStreak(days, today)).isEqualTo(10)
    }
}
