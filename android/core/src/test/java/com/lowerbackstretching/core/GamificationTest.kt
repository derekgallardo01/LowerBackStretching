package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class GamificationTest {

    // ----------- XP / levels -----------

    @Test fun `level 1 starts at 0 xp`() {
        assertThat(totalXpForLevel(1)).isEqualTo(0)
    }

    @Test fun `level 2 needs 100 xp`() {
        assertThat(totalXpForLevel(2)).isEqualTo(100)
    }

    @Test fun `level 5 needs 1000 xp`() {
        assertThat(totalXpForLevel(5)).isEqualTo(1000)
    }

    @Test fun `xpForSession rounds down`() {
        // 0.1 xp/sec → 295 sec = 29.5 → 29
        assertThat(xpForSession(295)).isEqualTo(29)
        assertThat(xpForSession(0)).isEqualTo(0)
        assertThat(xpForSession(-100)).isEqualTo(0)
    }

    @Test fun `levelFor handles boundary values exactly`() {
        assertThat(levelFor(0)).isEqualTo(1)
        assertThat(levelFor(99)).isEqualTo(1)
        assertThat(levelFor(100)).isEqualTo(2)
        assertThat(levelFor(299)).isEqualTo(2)
        assertThat(levelFor(300)).isEqualTo(3)
        assertThat(levelFor(1000)).isEqualTo(5)
    }

    @Test fun `xpProgress reports partial progress correctly`() {
        val p = xpProgress(150) // level 2 at +50 of 200 to L3
        assertThat(p.level).isEqualTo(2)
        assertThat(p.xpIntoLevel).isEqualTo(50)
        assertThat(p.xpToNextLevel).isEqualTo(200)
        assertThat(p.progress).isWithin(1e-3f).of(0.25f)
    }

    // ----------- Streaks -----------

    @Test fun `longestStreak for empty set is 0`() {
        assertThat(longestStreak(emptySet())).isEqualTo(0)
    }

    @Test fun `longestStreak for single day is 1`() {
        assertThat(longestStreak(setOf(LocalDate.of(2025, 1, 1)))).isEqualTo(1)
    }

    @Test fun `longestStreak picks the longer of two runs`() {
        val days = setOf(
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2),                          // 2-day run
            LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 11), LocalDate.of(2025, 1, 12), // 3-day run
            LocalDate.of(2025, 1, 20),                                                     // 1-day run
        )
        assertThat(longestStreak(days)).isEqualTo(3)
    }

    @Test fun `longestStreak counts every consecutive day`() {
        val days = (1..7).map { LocalDate.of(2025, 1, it) }.toSet()
        assertThat(longestStreak(days)).isEqualTo(7)
    }

    // ----------- Weekly / monthly -----------

    @Test fun `weeklyCompletions counts only days in the same iso week`() {
        // Pin today to Wednesday 2025-01-15.
        val today = LocalDate.of(2025, 1, 15)
        val days = setOf(
            LocalDate.of(2025, 1, 13), // Monday — same week
            LocalDate.of(2025, 1, 15), // Wed
            LocalDate.of(2025, 1, 6),  // prior week
        )
        assertThat(weeklyCompletions(days, today)).isEqualTo(2)
    }

    @Test fun `monthlyCompletions counts only the calendar month`() {
        val today = LocalDate.of(2025, 1, 15)
        val days = setOf(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 31),
            LocalDate.of(2024, 12, 31),
            LocalDate.of(2025, 2, 1),
        )
        assertThat(monthlyCompletions(days, today)).isEqualTo(2)
    }
}
