package com.lowerbackstretching.notifications

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class ShouldNudgeStreakTest {

    private val today = LocalDate.of(2026, 5, 25)
    private val todayEpochDay = today.toEpochDay()

    @Test
    fun returns_false_when_user_has_opted_out() = runTest {
        val result = shouldNudgeStreak(
            enabled = false,
            lastSessionEpochDay = todayEpochDay - 1,
            today = today,
            streakProvider = { 10 },
        )
        assertThat(result).isFalse()
    }

    @Test
    fun returns_false_when_user_already_stretched_today() = runTest {
        var streakProviderCalled = false
        val result = shouldNudgeStreak(
            enabled = true,
            lastSessionEpochDay = todayEpochDay,
            today = today,
            streakProvider = { streakProviderCalled = true; 10 },
        )
        assertThat(result).isFalse()
        // Today-short-circuit: don't pay the DB query.
        assertThat(streakProviderCalled).isFalse()
    }

    @Test
    fun returns_false_when_streak_is_below_threshold() = runTest {
        val result = shouldNudgeStreak(
            enabled = true,
            lastSessionEpochDay = todayEpochDay - 1,
            today = today,
            streakProvider = { 2 },
        )
        assertThat(result).isFalse()
    }

    @Test
    fun returns_true_when_streak_at_risk_and_user_opted_in() = runTest {
        val result = shouldNudgeStreak(
            enabled = true,
            lastSessionEpochDay = todayEpochDay - 1,
            today = today,
            streakProvider = { 3 },
        )
        assertThat(result).isTrue()
    }

    @Test
    fun returns_true_for_long_streak() = runTest {
        val result = shouldNudgeStreak(
            enabled = true,
            lastSessionEpochDay = todayEpochDay - 1,
            today = today,
            streakProvider = { 365 },
        )
        assertThat(result).isTrue()
    }

    @Test
    fun never_stretched_with_zero_default_returns_false() = runTest {
        // lastSessionEpochDay defaults to 0 — equivalent to "user never stretched".
        // Streak provider would return 0 in that case.
        val result = shouldNudgeStreak(
            enabled = true,
            lastSessionEpochDay = 0L,
            today = today,
            streakProvider = { 0 },
        )
        assertThat(result).isFalse()
    }
}
