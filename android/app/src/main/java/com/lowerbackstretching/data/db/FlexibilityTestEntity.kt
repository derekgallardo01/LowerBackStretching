package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per flexibility self-test snapshot. Each measurement is
 * nullable so the user can skip individual tests. Values are stored
 * in centimeters (the UI converts on the way in if needed).
 */
@Entity(tableName = "flexibility_tests")
data class FlexibilityTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAtEpochMillis: Long,
    val sitAndReachCm: Float?,
    val toeTouchCm: Float?,
    val shoulderReachCm: Float?,
)
