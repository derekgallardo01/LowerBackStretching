package com.lowerbackstretching.data

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Pure calculation functions for the gamification layer. Every function
 * here is total — no I/O, no time-of-day dependency unless passed in
 * explicitly. The view layer combines these with reactive Flows of
 * sessions/prefs and renders the result.
 *
 * Conventions:
 *  - "days" is always a `Set<LocalDate>` of distinct completed days.
 *  - "today" is always a parameter (default `LocalDate.now()`) so tests
 *    can pin it.
 *  - All counts/levels are `Int`; progress fractions are `Float`.
 */

/** XP earned per stretching second. A typical 5-minute routine = 30 XP. */
const val XP_PER_SECOND: Float = 0.1f

/**
 * Total XP required to *reach* a given level. Level 1 is the starting
 * level at 0 XP; the curve adds 100 XP for L2, 200 XP for L3, etc.
 *
 *   L1=0, L2=100, L3=300, L4=600, L5=1000, ...
 */
fun totalXpForLevel(level: Int): Int {
    val l = (level - 1).coerceAtLeast(0)
    return 50 * l * (l + 1)
}

fun xpForSession(durationSeconds: Int): Int =
    (durationSeconds.coerceAtLeast(0) * XP_PER_SECOND).toInt()

fun levelFor(totalXp: Int): Int {
    if (totalXp <= 0) return 1
    var level = 1
    while (totalXpForLevel(level + 1) <= totalXp) level++
    return level
}

data class XpProgress(
    val level: Int,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpToNextLevel: Int,
) {
    /** 0.0 at the start of the level, 1.0 just before leveling up. */
    val progress: Float
        get() = if (xpToNextLevel <= 0) 1f else xpIntoLevel.toFloat() / xpToNextLevel
}

fun xpProgress(totalXp: Int): XpProgress {
    val safe = totalXp.coerceAtLeast(0)
    val level = levelFor(safe)
    val base = totalXpForLevel(level)
    val next = totalXpForLevel(level + 1)
    return XpProgress(
        level = level,
        totalXp = safe,
        xpIntoLevel = safe - base,
        xpToNextLevel = next - base,
    )
}

/** Longest run of consecutive completed days, ever. */
fun longestStreak(days: Set<LocalDate>): Int {
    if (days.isEmpty()) return 0
    val sorted = days.sorted()
    var longest = 1
    var current = 1
    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1].plusDays(1)) {
            current++
            if (current > longest) longest = current
        } else {
            current = 1
        }
    }
    return longest
}

fun weeklyCompletions(days: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
    val wf = WeekFields.of(Locale.getDefault())
    val week = today.get(wf.weekOfWeekBasedYear())
    val year = today.get(wf.weekBasedYear())
    return days.count { d ->
        d.get(wf.weekOfWeekBasedYear()) == week && d.get(wf.weekBasedYear()) == year
    }
}

fun monthlyCompletions(days: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int =
    days.count { it.year == today.year && it.month == today.month }
