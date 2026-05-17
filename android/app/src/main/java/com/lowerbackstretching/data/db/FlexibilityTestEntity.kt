package com.lowerbackstretching.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lowerbackstretching.core.FlexibilityMeasurement

/**
 * One row per flexibility self-test snapshot. Each measurement is
 * nullable so the user can skip individual tests. Values are stored
 * in centimeters (the UI converts on the way in if needed).
 *
 * Implements the pure [FlexibilityMeasurement] contract so the delta
 * math in `:core` can compare snapshots without depending on Room.
 */
@Entity(tableName = "flexibility_tests")
data class FlexibilityTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAtEpochMillis: Long,
    override val sitAndReachCm: Float?,
    override val toeTouchCm: Float?,
    override val shoulderReachCm: Float?,
) : FlexibilityMeasurement
