package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FlexibilityTest {

    private data class Measurement(
        override val sitAndReachCm: Float?,
        override val toeTouchCm: Float?,
        override val shoulderReachCm: Float?,
    ) : FlexibilityMeasurement

    @Test
    fun `returns null deltas when either snapshot is missing`() {
        val now = Measurement(sitAndReachCm = 10f, toeTouchCm = null, shoulderReachCm = 5f)
        assertThat(flexibilityDelta(now, null).sitAndReachCm).isNull()
        assertThat(flexibilityDelta(null, now).toeTouchCm).isNull()
    }

    @Test
    fun `subtracts per-metric and skips when one side is null`() {
        val now  = Measurement(sitAndReachCm = 12f, toeTouchCm = null, shoulderReachCm = 7f)
        val prev = Measurement(sitAndReachCm = 10f, toeTouchCm = 3f,   shoulderReachCm = null)
        val delta = flexibilityDelta(now, prev)
        assertThat(delta.sitAndReachCm).isEqualTo(2f)
        assertThat(delta.toeTouchCm).isNull()
        assertThat(delta.shoulderReachCm).isNull()
    }
}
