package com.lowerbackstretching.data

import com.lowerbackstretching.data.db.SessionDao
import com.lowerbackstretching.data.db.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SessionRepository(private val dao: SessionDao) {

    fun completedDays(): Flow<Set<LocalDate>> =
        dao.completedDays().map { list -> list.map { LocalDate.ofEpochDay(it) }.toSet() }

    fun count(): Flow<Int> = dao.count()

    fun streak(): Flow<Int> = completedDays().map { computeStreak(it) }

    suspend fun recordCompletion(programId: String, day: Int, durationSeconds: Int) {
        val now = System.currentTimeMillis()
        dao.insert(
            SessionEntity(
                programId = programId,
                dayNumber = day,
                completedAtEpochDay = LocalDate.now().toEpochDay(),
                completedAtEpochMillis = now,
                durationSeconds = durationSeconds,
            )
        )
    }

    private fun computeStreak(days: Set<LocalDate>): Int {
        if (days.isEmpty()) return 0
        var streak = 0
        var cursor = LocalDate.now()
        // Allow today to be missing (streak only breaks if yesterday is also missing)
        if (cursor !in days) cursor = cursor.minusDays(1)
        while (cursor in days) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
