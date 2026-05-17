package com.lowerbackstretching.core

import com.lowerbackstretching.core.model.Stretch

/**
 * Helpers for the `bodyParts: List<String>` field on [Stretch]. Strings on
 * disk are kebab-case (`lower-back`); humans see them as `lower back`.
 */
object BodyParts {

    /** "lower-back" -> "lower back" */
    fun display(part: String): String = part.replace('-', ' ')

    fun displayList(parts: List<String>, separator: String = " · "): String =
        parts.joinToString(separator) { display(it) }

    /** All unique body parts present in the catalog, sorted. */
    fun distinctSorted(stretches: List<Stretch>): List<String> =
        stretches.flatMap { it.bodyParts }.distinct().sorted()

    /** Same as [distinctSorted] but with "all" prepended for filter UIs. */
    fun filterOptions(stretches: List<Stretch>): List<String> =
        listOf(ALL) + distinctSorted(stretches)

    const val ALL = "all"
}
