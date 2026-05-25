package com.lowerbackstretching.data

import com.lowerbackstretching.core.PainContext
import com.lowerbackstretching.data.db.PainLogDao
import com.lowerbackstretching.data.db.PainLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

class PainLogRepository(private val dao: PainLogDao) {

    fun all(): Flow<List<PainLogEntity>> = dao.all()

    fun latest(): Flow<PainLogEntity?> = dao.latest()

    suspend fun recordPre(painLevel: Int, bodyLocationTag: String?): Long =
        dao.insert(
            PainLogEntity(
                recordedAtEpochMillis = System.currentTimeMillis(),
                painLevel = painLevel,
                bodyLocationTag = bodyLocationTag,
                context = PainContext.PRE_SESSION,
                sessionId = null,
            )
        )

    suspend fun recordPost(painLevel: Int, bodyLocationTag: String?, sessionId: Long): Long =
        dao.insert(
            PainLogEntity(
                recordedAtEpochMillis = System.currentTimeMillis(),
                painLevel = painLevel,
                bodyLocationTag = bodyLocationTag,
                context = PainContext.POST_SESSION,
                sessionId = sessionId,
            )
        )

    suspend fun delete(log: PainLogEntity) = dao.delete(log)

    /**
     * True when a PRE_SESSION rating was already recorded in the
     * device-local calendar day containing [nowEpochMillis]. Used to
     * gate the pre-session prompt to "first session of the day".
     */
    suspend fun hasPreLoggedToday(nowEpochMillis: Long = System.currentTimeMillis()): Boolean =
        dao.hasPreLogSince(startOfDayEpochMillis(nowEpochMillis))

    private fun startOfDayEpochMillis(nowEpochMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(nowEpochMillis).atZone(zone).toLocalDate()
        return today.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
