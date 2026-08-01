package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Covers the finish-of-session decisions previously buried in
 * `PlayerViewModel`, which no JVM test could reach.
 */
class SessionCompletionTest {
    // ---------- crossedMilestone ----------

    @Test
    fun crossing_a_threshold_returns_it() {
        assertThat(crossedMilestone(streakBefore = 6, streakAfter = 7, alreadyShown = emptySet()))
            .isEqualTo(7)
    }

    @Test
    fun landing_below_a_threshold_returns_null() {
        assertThat(crossedMilestone(streakBefore = 5, streakAfter = 6, alreadyShown = emptySet()))
            .isNull()
    }

    @Test
    fun staying_above_a_threshold_does_not_re_fire() {
        // Day 8 → 9: already past 7, nothing new crossed.
        assertThat(crossedMilestone(streakBefore = 8, streakAfter = 9, alreadyShown = emptySet()))
            .isNull()
    }

    @Test
    fun an_already_shown_milestone_is_suppressed() {
        // Streak broken and rebuilt back to 7 — don't celebrate twice.
        assertThat(crossedMilestone(streakBefore = 6, streakAfter = 7, alreadyShown = setOf(7)))
            .isNull()
    }

    @Test
    fun a_later_threshold_still_fires_after_an_earlier_one_was_shown() {
        assertThat(crossedMilestone(streakBefore = 29, streakAfter = 30, alreadyShown = setOf(7)))
            .isEqualTo(30)
    }

    @Test
    fun every_declared_threshold_can_fire() {
        for (t in MILESTONE_THRESHOLDS) {
            assertThat(crossedMilestone(t - 1, t, emptySet())).isEqualTo(t)
        }
    }

    @Test
    fun multiple_crossings_report_the_lowest_unshown_threshold() {
        // Pathological jump (e.g. a data import): 6 → 40 crosses both 7 and 30.
        assertThat(crossedMilestone(streakBefore = 6, streakAfter = 40, alreadyShown = emptySet()))
            .isEqualTo(7)
        // With 7 already acknowledged, the next one up is reported.
        assertThat(crossedMilestone(streakBefore = 6, streakAfter = 40, alreadyShown = setOf(7)))
            .isEqualTo(30)
    }

    @Test
    fun a_streak_reset_to_zero_crosses_nothing() {
        assertThat(crossedMilestone(streakBefore = 10, streakAfter = 0, alreadyShown = emptySet()))
            .isNull()
    }

    // ---------- newlyUnlocked ----------

    private fun status(
        id: AchievementId,
        unlocked: Boolean,
    ) = AchievementStatus(
        achievement = Achievements.all.first { it.id == id },
        progress = if (unlocked) 100 else 0,
        unlocked = unlocked,
    )

    @Test
    fun an_achievement_that_flips_to_unlocked_is_reported() {
        val id = Achievements.all.first().id
        val result = newlyUnlocked(
            before = listOf(status(id, unlocked = false)),
            after = listOf(status(id, unlocked = true)),
        )
        assertThat(result.map { it.id }).containsExactly(id)
    }

    @Test
    fun an_achievement_already_unlocked_is_not_re_announced() {
        val id = Achievements.all.first().id
        val result = newlyUnlocked(
            before = listOf(status(id, unlocked = true)),
            after = listOf(status(id, unlocked = true)),
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun an_achievement_still_locked_is_not_reported() {
        val id = Achievements.all.first().id
        val result = newlyUnlocked(
            before = listOf(status(id, unlocked = false)),
            after = listOf(status(id, unlocked = false)),
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun several_unlocking_at_once_are_all_reported() {
        val ids = Achievements.all.take(3).map { it.id }
        val result = newlyUnlocked(
            before = ids.map { status(it, unlocked = false) },
            after = ids.map { status(it, unlocked = true) },
        )
        assertThat(result.map { it.id }).containsExactlyElementsIn(ids)
    }

    // ---------- resumeIndex ----------

    @Test
    fun no_saved_session_starts_at_zero() {
        assertThat(resumeIndex(null, "lower-back-relief-7day", 1)).isEqualTo(0)
    }

    @Test
    fun a_matching_saved_session_resumes_at_its_index() {
        val saved = InProgressSession("lower-back-relief-7day", dayNumber = 3, index = 4)
        assertThat(resumeIndex(saved, "lower-back-relief-7day", 3)).isEqualTo(4)
    }

    @Test
    fun a_different_program_starts_at_zero() {
        val saved = InProgressSession("lower-back-relief-7day", dayNumber = 3, index = 4)
        assertThat(resumeIndex(saved, "sciatica-relief-10day", 3)).isEqualTo(0)
    }

    @Test
    fun a_different_day_of_the_same_program_starts_at_zero() {
        val saved = InProgressSession("lower-back-relief-7day", dayNumber = 3, index = 4)
        assertThat(resumeIndex(saved, "lower-back-relief-7day", 4)).isEqualTo(0)
    }

    @Test
    fun synthetic_routine_ids_resume_on_day_zero() {
        val pid = SyntheticProgramId.routine(7L)
        val saved = InProgressSession(pid, dayNumber = 0, index = 2)
        assertThat(resumeIndex(saved, pid, 0)).isEqualTo(2)
    }
}
