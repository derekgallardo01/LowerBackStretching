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
    fun typeFor(programId: String): SessionType =
        when {
            programId.startsWith(SINGLE_PREFIX) -> SessionType.SINGLE
            programId.startsWith(ROUTINE_PREFIX) -> SessionType.ROUTINE
            else -> SessionType.PROGRAM
        }

    /**
     * Inverse of [single] — the stretch id encoded in a synthetic
     * single-stretch programId, or null if this isn't one (or the
     * prefix is all there is).
     */
    fun stretchIdFrom(programId: String): String? =
        programId
            .removePrefix(SINGLE_PREFIX)
            .takeIf { programId.startsWith(SINGLE_PREFIX) && it.isNotEmpty() }

    /**
     * Inverse of [routine] — the routine row id encoded in a synthetic
     * routine programId, or null if this isn't one or the suffix isn't
     * a valid Long.
     */
    fun routineIdFrom(programId: String): Long? =
        if (programId.startsWith(ROUTINE_PREFIX)) {
            programId.removePrefix(ROUTINE_PREFIX).toLongOrNull()
        } else {
            null
        }
}

enum class SessionType(
    val storageValue: String,
) {
    PROGRAM("program"),
    SINGLE("single"),
    ROUTINE("routine"),
    ;

    companion object {
        fun fromStorage(value: String): SessionType = entries.firstOrNull { it.storageValue == value } ?: PROGRAM
    }
}
