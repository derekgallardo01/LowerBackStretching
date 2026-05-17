package com.lowerbackstretching.data

import com.lowerbackstretching.core.SyntheticProgramId
import com.lowerbackstretching.core.computeStreak
import com.lowerbackstretching.core.longestStreak
import com.lowerbackstretching.data.db.SessionDao
import com.lowerbackstretching.data.db.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SessionRepository(private val dao: SessionDao) {

    fun completedDays(): Flow<Set<LocalDate>> =
        dao.completedDays().map { list -> list.map { LocalDate.ofEpochDay(it) }.toSet() }

    fun count(): Flow<Int> = dao.count()

    fun streak(): Flow<Int> = completedDays().map { computeStreak(it, LocalDate.now()) }

    fun longestStreak(): Flow<Int> = completedDays().map { longestStreak(it) }

    fun totalDurationSeconds(): Flow<Int> = dao.totalDurationSeconds().map { it ?: 0 }

    fun recent(limit: Int = 20): Flow<List<SessionEntity>> =
        dao.recent(limit)

    suspend fun recordCompletion(programId: String, day: Int, durationSeconds: Int) {
        val now = System.currentTimeMillis()
        dao.insert(
            SessionEntity(
                programId = programId,
                dayNumber = day,
                completedAtEpochDay = LocalDate.now().toEpochDay(),
                completedAtEpochMillis = now,
                durationSeconds = durationSeconds,
                type = SyntheticProgramId.typeFor(programId).storageValue,
            )
        )
    }
}
