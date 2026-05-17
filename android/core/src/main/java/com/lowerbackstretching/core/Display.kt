package com.lowerbackstretching.core

import com.lowerbackstretching.core.model.Program
import com.lowerbackstretching.core.model.ProgramDay
import com.lowerbackstretching.core.model.Stretch

/**
 * User-facing strings derived from the catalog models. Pure functions
 * over :core types — the watch app and any future shared surface can
 * call these without touching DataStore or Room. Mirrors iOS
 * `Stretch+Display.swift`.
 */

/** "Easy" (capitalized for display from the on-disk "easy"). */
val Stretch.difficultyDisplay: String
    get() = difficulty.replaceFirstChar(Char::titlecase)

/**
 * Per-stretch duration formatted for display.
 * - [DurationUnit.SECONDS]: "30s"
 * - [DurationUnit.MINUTES_SHORT]: "0:30" / "1:00" / "1:30"
 */
fun formatDuration(seconds: Int, unit: DurationUnit): String = when (unit) {
    DurationUnit.SECONDS -> "${seconds}s"
    DurationUnit.MINUTES_SHORT -> {
        val m = seconds / 60
        val s = seconds % 60
        "$m:${s.toString().padStart(2, '0')}"
    }
}

/** "30s · Easy · lower back · spine" */
fun Stretch.shortSubtitle(unit: DurationUnit = DurationUnit.SECONDS): String =
    "${formatDuration(durationSeconds, unit)} · $difficultyDisplay · ${BodyParts.displayList(bodyParts)}"

/** Filter by a body part. Pass [BodyParts.ALL] to return everything. */
fun List<Stretch>.filteredBy(bodyPart: String): List<Stretch> =
    if (bodyPart == BodyParts.ALL) this else filter { bodyPart in it.bodyParts }

/** "7-day · lower back" */
val Program.subtitle: String
    get() = "${days.size}-day · ${category.replace('-', ' ')}"

/** "Day 1 · Gentle Start" */
val ProgramDay.headerTitle: String
    get() = "Day $day · $title"

/** "5 stretches · 3 min" */
fun ProgramDay.subtitle(totalSeconds: Int): String =
    "${stretchIds.size} stretches · ${totalSeconds / 60} min"

/**
 * "5 stretches · 3 min" — pure helper for any UI row that knows the
 * stretch count + a precomputed total duration. Used by custom-routine
 * cards which have a Room-bound type living outside :core.
 */
fun stretchCountSubtitle(stretchCount: Int, totalSeconds: Int): String =
    "$stretchCount stretches · ${totalSeconds / 60} min"
