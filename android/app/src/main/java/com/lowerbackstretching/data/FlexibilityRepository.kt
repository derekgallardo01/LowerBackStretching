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
