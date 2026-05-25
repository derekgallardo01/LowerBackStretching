package com.lowerbackstretching.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PainTest {

    private data class Log(
        override val recordedAtEpochMillis: Long,
        override val painLevel: Int,
        override val bodyLocationTag: String? = null,
        override val context: String,
    ) : PainMeasurement

    @Test
    fun `sessionPainDelta computes post minus pre`() {
        val pair = SessionPainPair(
            pre = Log(0, 7, context = PainContext.PRE_SESSION),
            post = Log(1, 4, context = PainContext.POST_SESSION),
        )
        val d = sessionPainDelta(pair)
        assertThat(d.pre).isEqualTo(7)
        assertThat(d.post).isEqualTo(4)
        assertThat(d.delta).isEqualTo(-3)
    }

    @Test
    fun `sessionPainDelta returns null delta when pre is missing`() {
        val pair = SessionPainPair(
            pre = null,
            post = Log(1, 4, context = PainContext.POST_SESSION),
        )
        val d = sessionPainDelta(pair)
        assertThat(d.pre).isNull()
        assertThat(d.delta).isNull()
        assertThat(d.post).isEqualTo(4)
    }

    @Test
    fun `pairSessionPainLogs picks nearest preceding pre within window`() {
        val tooEarly = Log(0L, 8, context = PainContext.PRE_SESSION)
        val nearestPre = Log(1_000L, 6, context = PainContext.PRE_SESSION)
        val post = Log(2_000L, 3, context = PainContext.POST_SESSION)

        val pairs = pairSessionPainLogs(
            logs = listOf(post, tooEarly, nearestPre),
            lookbackMillis = 5_000L,
        )

        assertThat(pairs).hasSize(1)
        assertThat(pairs[0].post).isEqualTo(post)
        assertThat(pairs[0].pre).isEqualTo(nearestPre)
    }

    @Test
    fun `pairSessionPainLogs returns null pre when none inside lookback window`() {
        val outsideWindow = Log(0L, 8, context = PainContext.PRE_SESSION)
        val post = Log(10_000L, 4, context = PainContext.POST_SESSION)

        val pairs = pairSessionPainLogs(
            logs = listOf(outsideWindow, post),
            lookbackMillis = 5_000L,
        )

        assertThat(pairs).hasSize(1)
        assertThat(pairs[0].pre).isNull()
        assertThat(pairs[0].post).isEqualTo(post)
    }

    @Test
    fun `pairSessionPainLogs treats lookback boundary as inclusive`() {
        val preExactlyAtBoundary = Log(0L, 5, context = PainContext.PRE_SESSION)
        val post = Log(5_000L, 5, context = PainContext.POST_SESSION)

        val pairs = pairSessionPainLogs(
            logs = listOf(preExactlyAtBoundary, post),
            lookbackMillis = 5_000L,
        )

        assertThat(pairs[0].pre).isEqualTo(preExactlyAtBoundary)
    }

    @Test
    fun `pairSessionPainLogs ignores pre logs newer than post`() {
        val laterPre = Log(2_000L, 2, context = PainContext.PRE_SESSION)
        val post = Log(1_000L, 5, context = PainContext.POST_SESSION)

        val pairs = pairSessionPainLogs(
            logs = listOf(laterPre, post),
            lookbackMillis = 5_000L,
        )

        assertThat(pairs[0].pre).isNull()
    }

    @Test
    fun `pairSessionPainLogs sorts results newest post first`() {
        val pre1 = Log(0L, 7, context = PainContext.PRE_SESSION)
        val post1 = Log(100L, 5, context = PainContext.POST_SESSION)
        val pre2 = Log(1_000L, 6, context = PainContext.PRE_SESSION)
        val post2 = Log(1_100L, 4, context = PainContext.POST_SESSION)

        val pairs = pairSessionPainLogs(
            logs = listOf(pre1, post1, pre2, post2),
            lookbackMillis = 5_000L,
        )

        assertThat(pairs).hasSize(2)
        assertThat(pairs[0].post).isEqualTo(post2)
        assertThat(pairs[1].post).isEqualTo(post1)
    }
}
