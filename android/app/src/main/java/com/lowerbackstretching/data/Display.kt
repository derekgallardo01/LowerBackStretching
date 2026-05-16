package com.lowerbackstretching.data

import com.lowerbackstretching.data.db.CustomRoutineEntity
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.data.model.ProgramDay
import com.lowerbackstretching.data.model.Stretch

/**
 * User-facing strings derived from models. Lives next to the model layer
 * so both [ContentRepository] consumers and Compose screens can use them.
 * Mirrors iOS `Stretch+Display.swift`.
 */

/** "30s · easy · lower back · spine" */
val Stretch.shortSubtitle: String
    get() = "${durationSeconds}s · $difficulty · ${BodyParts.displayList(bodyParts)}"

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

/** "5 stretches · 3 min" */
fun CustomRoutineEntity.subtitle(totalSeconds: Int): String =
    "${stretchIds.size} stretches · ${totalSeconds / 60} min"
