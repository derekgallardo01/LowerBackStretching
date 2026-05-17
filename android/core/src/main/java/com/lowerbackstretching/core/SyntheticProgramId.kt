package com.lowerbackstretching.core

/**
 * Synthetic programId values stored on `SessionRecord` for sessions
 * that weren't part of a real `Program` — single-stretch practice and
 * user-created custom routines.
 *
 * These strings never round-trip through `ContentRepository.program(id)`
 * (it returns null, and the UI falls back to showing the raw id). They
 * just need to be unique enough to identify the source.
 */
object SyntheticProgramId {

    const val SINGLE_PREFIX = "single-"
    const val ROUTINE_PREFIX = "routine-"

    fun single(stretchId: String) = "$SINGLE_PREFIX$stretchId"
    fun routine(routineId: Long) = "$ROUTINE_PREFIX$routineId"

    /**
     * Classify a stored programId so consumers (gamification, stats,
     * etc.) can filter by source without parsing the raw string.
     */
    fun typeFor(programId: String): SessionType = when {
        programId.startsWith(SINGLE_PREFIX) -> SessionType.SINGLE
        programId.startsWith(ROUTINE_PREFIX) -> SessionType.ROUTINE
        else -> SessionType.PROGRAM
    }
}

enum class SessionType(val storageValue: String) {
    PROGRAM("program"),
    SINGLE("single"),
    ROUTINE("routine");

    companion object {
        fun fromStorage(value: String): SessionType =
            entries.firstOrNull { it.storageValue == value } ?: PROGRAM
    }
}
