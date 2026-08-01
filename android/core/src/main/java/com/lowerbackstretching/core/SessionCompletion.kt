package com.lowerbackstretching.core

/*
 * Pure decision logic for the moment a routine finishes.
 *
 * The Android `PlayerViewModel` orchestrates this — read stats, record the
 * session, re-read stats, then decide what the finish screen celebrates. The
 * *decisions* in that sequence are ordinary functions of before/after values, so
 * they live here where they can be unit-tested without an Application, a
 * database, or Robolectric. Mirrors how `shouldNudgeStreak` was pulled out of
 * `StreakNudgeReceiver` for the same reason.
 */

/** Streak thresholds that earn a milestone modal. First crossing only. */
val MILESTONE_THRESHOLDS = listOf(7, 30, 100, 365)

/**
 * The milestone to celebrate, or null if this session didn't cross one.
 *
 * A threshold counts as crossed when the streak was strictly below it before the
 * session and is at or above it after. [alreadyShown] suppresses repeats, so a
 * user who breaks and rebuilds a 7-day streak doesn't see the same modal twice.
 *
 * Returns the *lowest* unshown threshold crossed — a single session can only
 * advance the streak by one day, so more than one crossing means earlier
 * milestones were missed (e.g. notifications off), and the smaller one is the
 * more meaningful thing to acknowledge first.
 */
fun crossedMilestone(
    streakBefore: Int,
    streakAfter: Int,
    alreadyShown: Set<Int>,
    thresholds: List<Int> = MILESTONE_THRESHOLDS,
): Int? =
    thresholds.sorted().firstOrNull { t ->
        streakBefore < t && streakAfter >= t && t !in alreadyShown
    }

/**
 * Achievements that flipped from locked to unlocked across this session.
 *
 * Compares by id and only reports ones that were genuinely locked in [before],
 * so an achievement already unlocked in an earlier session is never re-announced.
 */
fun newlyUnlocked(
    before: List<AchievementStatus>,
    after: List<AchievementStatus>,
): List<Achievement> {
    val previouslyLocked = before.filter { !it.unlocked }.map { it.achievement.id }.toSet()
    return after
        .filter { it.unlocked && it.achievement.id in previouslyLocked }
        .map { it.achievement }
}

/**
 * Where the player should start, given whatever partial session was persisted.
 *
 * Only resumes when the saved session refers to the same source — otherwise
 * starting the "Desk worker" routine after abandoning day 3 of a program would
 * drop the user into the middle of the wrong stretch list.
 */
fun resumeIndex(
    saved: InProgressSession?,
    programId: String,
    dayNumber: Int,
): Int =
    if (saved != null && saved.programId == programId && saved.dayNumber == dayNumber) {
        saved.index
    } else {
        0
    }
