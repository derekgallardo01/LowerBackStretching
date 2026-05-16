package com.lowerbackstretching.data

/**
 * Synthetic programId values stored on `SessionRecord` for sessions that
 * weren't part of a real `Program` — single-stretch practice and
 * user-created custom routines.
 *
 * These strings never round-trip through `ContentRepository.program(id)`
 * (it returns null, and the UI falls back to showing the raw id). They
 * just need to be unique enough to identify the source.
 */
object SyntheticProgramId {
    fun single(stretchId: String) = "single-$stretchId"
    fun routine(routineId: Long) = "routine-$routineId"
}
