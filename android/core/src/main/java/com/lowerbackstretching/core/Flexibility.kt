package com.lowerbackstretching.core

/**
 * The three measurements captured by the flexibility self-test, in
 * centimeters. Pure value contract — the Room-backed
 * `FlexibilityTestEntity` in `:app` implements this so the delta math
 * here can compare snapshots without dragging the entity (or Room
 * itself) into `:core`.
 */
interface FlexibilityMeasurement {
    val sitAndReachCm: Float?
    val toeTouchCm: Float?
    val shoulderReachCm: Float?
}

/** Per-metric difference between two snapshots; null when either side lacks that metric. */
data class FlexibilityDelta(
    val sitAndReachCm: Float?,
    val toeTouchCm: Float?,
    val shoulderReachCm: Float?,
)

/**
 * Delta between [latest] and [previous] for each measurement. Returns
 * an all-null delta when either snapshot is missing. Used by the
 * history view to render "+2.0 cm vs last test" indicators.
 */
fun flexibilityDelta(
    latest: FlexibilityMeasurement?,
    previous: FlexibilityMeasurement?,
): FlexibilityDelta {
    if (latest == null || previous == null) return FlexibilityDelta(null, null, null)
    return FlexibilityDelta(
        sitAndReachCm = subtractOrNull(latest.sitAndReachCm, previous.sitAndReachCm),
        toeTouchCm = subtractOrNull(latest.toeTouchCm, previous.toeTouchCm),
        shoulderReachCm = subtractOrNull(latest.shoulderReachCm, previous.shoulderReachCm),
    )
}

private fun subtractOrNull(a: Float?, b: Float?): Float? =
    if (a != null && b != null) a - b else null
