package com.lowerbackstretching.data

import com.lowerbackstretching.data.db.ProgramProgressDao
import com.lowerbackstretching.data.db.ProgramProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgramProgressRepository(private val dao: ProgramProgressDao) {

    /**
     * The next day the user should do for [programId]. Defaults to 1
     * (the first day) if the program has never been started.
     */
    fun currentDay(programId: String): Flow<Int> =
        dao.observe(programId).map { it?.currentDay ?: 1 }

    /**
     * Advance the bookmark after a completion. The next day is one
     * past [completedDay], capped at [totalDays] + 1 (so the caller
     * can detect "program done" by reading > totalDays). Synthetic
     * program ids (single-* / routine-*) are ignored.
     */
    suspend fun advance(programId: String, completedDay: Int, totalDays: Int) {
        if (SyntheticProgramId.typeFor(programId) != SessionType.PROGRAM) return
        val next = (completedDay + 1).coerceAtMost(totalDays + 1)
        dao.upsert(
            ProgramProgressEntity(
                programId = programId,
                currentDay = next,
                updatedAtEpochMillis = System.currentTimeMillis(),
            )
        )
    }

    suspend fun reset(programId: String) {
        dao.deleteFor(programId)
    }
}
