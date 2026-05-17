package com.lowerbackstretching.data

enum class AchievementId {
    FIRST_SESSION,
    SEVEN_DAY_STREAK,
    THIRTY_DAY_STREAK,
    FIFTY_SESSIONS,
    HUNDRED_SESSIONS,
    LEVEL_FIVE,
    LEVEL_TEN,
}

data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
    /** N events required. 1 = binary unlock. */
    val target: Int,
)

data class AchievementStatus(
    val achievement: Achievement,
    val progress: Int,
    val unlocked: Boolean,
)

object Achievements {
    val all: List<Achievement> = listOf(
        Achievement(AchievementId.FIRST_SESSION, "First steps", "Complete your first stretching session.", 1),
        Achievement(AchievementId.SEVEN_DAY_STREAK, "On a roll", "Stretch 7 days in a row.", 7),
        Achievement(AchievementId.THIRTY_DAY_STREAK, "Habit forged", "Stretch 30 days in a row.", 30),
        Achievement(AchievementId.FIFTY_SESSIONS, "Half a hundred", "Complete 50 sessions.", 50),
        Achievement(AchievementId.HUNDRED_SESSIONS, "Centurion", "Complete 100 sessions.", 100),
        Achievement(AchievementId.LEVEL_FIVE, "Limber", "Reach level 5.", 5),
        Achievement(AchievementId.LEVEL_TEN, "Bendy", "Reach level 10.", 10),
    )
}

/**
 * Compute the status of every achievement from already-derived stats
 * (no DB access). Locked rows still carry a [progress] value so the UI
 * can render a "23 / 50" indicator under each badge.
 */
fun evaluateAchievements(
    totalSessions: Int,
    longestStreak: Int,
    level: Int,
): List<AchievementStatus> = Achievements.all.map { a ->
    val raw = when (a.id) {
        AchievementId.FIRST_SESSION,
        AchievementId.FIFTY_SESSIONS,
        AchievementId.HUNDRED_SESSIONS -> totalSessions
        AchievementId.SEVEN_DAY_STREAK,
        AchievementId.THIRTY_DAY_STREAK -> longestStreak
        AchievementId.LEVEL_FIVE,
        AchievementId.LEVEL_TEN -> level
    }
    AchievementStatus(
        achievement = a,
        progress = raw.coerceAtMost(a.target),
        unlocked = raw >= a.target,
    )
}
