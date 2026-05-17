package com.lowerbackstretching.data

import com.lowerbackstretching.data.db.FlexibilityTestDao
import com.lowerbackstretching.data.db.FlexibilityTestEntity
import kotlinx.coroutines.flow.Flow

class FlexibilityRepository(private val dao: FlexibilityTestDao) {

    fun all(): Flow<List<FlexibilityTestEntity>> = dao.all()

    fun latest(): Flow<FlexibilityTestEntity?> = dao.latest()

    suspend fun record(
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?,
    ): Long = dao.insert(
        FlexibilityTestEntity(
            recordedAtEpochMillis = System.currentTimeMillis(),
            sitAndReachCm = sitAndReachCm,
            toeTouchCm = toeTouchCm,
            shoulderReachCm = shoulderReachCm,
        )
    )

    suspend fun delete(test: FlexibilityTestEntity) = dao.delete(test)
}

/**
 * Pure helper: the delta between [latest] and [previous] for each
 * measurement. Used by the history view to show "+2.0 cm vs last test"
 * style indicators. Returns null per metric if either snapshot is
 * missing that metric.
 */
fun flexibilityDelta(
    latest: FlexibilityTestEntity?,
    previous: FlexibilityTestEntity?,
): FlexibilityDelta {
    if (latest == null || previous == null) return FlexibilityDelta(null, null, null)
    return FlexibilityDelta(
        sitAndReachCm = subtractOrNull(latest.sitAndReachCm, previous.sitAndReachCm),
        toeTouchCm = subtractOrNull(latest.toeTouchCm, previous.toeTouchCm),
        shoulderReachCm = subtractOrNull(latest.shoulderReachCm, previous.shoulderReachCm),
    )
}

data class FlexibilityDelta(
    val sitAndReachCm: Float?,
    val toeTouchCm: Float?,
    val shoulderReachCm: Float?,
)

private fun subtractOrNull(a: Float?, b: Float?): Float? =
    if (a != null && b != null) a - b else null
