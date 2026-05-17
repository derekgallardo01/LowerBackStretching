package com.lowerbackstretching.data

/**
 * Persistent record of a routine the user started but didn't finish.
 * At most one is stored at a time — starting a different routine
 * overwrites it. The viewmodel writes on every index change and clears
 * on completion. The player reads it on init to resume where the user
 * left off.
 *
 * Stored in DataStore via [Prefs.inProgressSession] / [Prefs.saveInProgress]
 * / [Prefs.clearInProgress].
 */
data class InProgressSession(
    val programId: String,
    val dayNumber: Int,
    val index: Int,
)
