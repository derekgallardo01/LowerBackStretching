package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AchievementsTest {

    @Test fun `nothing unlocked at zero stats`() {
        val statuses = evaluateAchievements(totalSessions = 0, longestStreak = 0, level = 1)
        assertThat(statuses.filter { it.unlocked }).isEmpty()
    }

    @Test fun `first session unlocks on the first completion`() {
        val statuses = evaluateAchievements(totalSessions = 1, longestStreak = 1, level = 1)
        val first = statuses.first { it.achievement.id == AchievementId.FIRST_SESSION }
        assertThat(first.unlocked).isTrue()
        assertThat(first.progress).isEqualTo(1)
    }

    @Test fun `seven day streak unlocks at seven`() {
        val statuses = evaluateAchievements(totalSessions = 10, longestStreak = 7, level = 1)
        val a = statuses.first { it.achievement.id == AchievementId.SEVEN_DAY_STREAK }
        assertThat(a.unlocked).isTrue()
    }

    @Test fun `locked achievement still reports progress up to target`() {
        val statuses = evaluateAchievements(totalSessions = 23, longestStreak = 3, level = 2)
        val fifty = statuses.first { it.achievement.id == AchievementId.FIFTY_SESSIONS }
        assertThat(fifty.unlocked).isFalse()
        assertThat(fifty.progress).isEqualTo(23)

        val seven = statuses.first { it.achievement.id == AchievementId.SEVEN_DAY_STREAK }
        assertThat(seven.unlocked).isFalse()
        assertThat(seven.progress).isEqualTo(3)
    }

    @Test fun `progress is capped at target so the UI bar doesn't overflow`() {
        val statuses = evaluateAchievements(totalSessions = 999, longestStreak = 999, level = 99)
        for (status in statuses) {
            assertThat(status.progress).isAtMost(status.achievement.target)
            assertThat(status.unlocked).isTrue()
        }
    }

    @Test fun `level achievements key off level not xp`() {
        val statuses = evaluateAchievements(totalSessions = 1, longestStreak = 0, level = 5)
        val levelFive = statuses.first { it.achievement.id == AchievementId.LEVEL_FIVE }
        val levelTen = statuses.first { it.achievement.id == AchievementId.LEVEL_TEN }
        assertThat(levelFive.unlocked).isTrue()
        assertThat(levelTen.unlocked).isFalse()
        assertThat(levelTen.progress).isEqualTo(5)
    }
}
