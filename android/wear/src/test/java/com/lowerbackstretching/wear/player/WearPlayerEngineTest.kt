package com.lowerbackstretching.wear.player

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.wear.model.WatchStretch
import org.junit.Test

class WearPlayerEngineTest {

    private fun stretch(id: String, seconds: Int) =
        WatchStretch(id = id, name = id, durationSeconds = seconds)

    @Test fun `initial state loads first stretch`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        val s = engine.state.value
        assertThat(s.index).isEqualTo(0)
        assertThat(s.remainingSeconds).isEqualTo(10)
        assertThat(s.running).isTrue()
        assertThat(s.finished).isFalse()
    }

    @Test fun `empty list marks finished`() {
        val engine = WearPlayerEngine(emptyList())
        assertThat(engine.state.value.finished).isTrue()
    }

    @Test fun `tick decrements remaining`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 3)))
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(2)
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(1)
    }

    @Test fun `tick at one advances to next`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 1), stretch("b", 5)))
        val finished = engine.tick()
        assertThat(finished).isFalse()
        assertThat(engine.state.value.index).isEqualTo(1)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(5)
    }

    @Test fun `tick past last finishes`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 1)))
        val finished = engine.tick()
        assertThat(finished).isTrue()
        assertThat(engine.state.value.finished).isTrue()
        assertThat(engine.state.value.running).isFalse()
    }

    @Test fun `tick when paused is noop`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10)))
        engine.togglePlay()
        engine.tick()
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test fun `next skips to following`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 30), stretch("b", 45)))
        engine.next()
        assertThat(engine.state.value.index).isEqualTo(1)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(45)
    }

    @Test fun `previous at zero stays`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        engine.previous()
        assertThat(engine.state.value.index).isEqualTo(0)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test fun `previous resets remaining`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        engine.next()
        engine.tick()
        engine.previous()
        assertThat(engine.state.value.index).isEqualTo(0)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test fun `toggle play flips running`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10)))
        assertThat(engine.state.value.running).isTrue()
        engine.togglePlay()
        assertThat(engine.state.value.running).isFalse()
        engine.togglePlay()
        assertThat(engine.state.value.running).isTrue()
    }

    @Test fun `progress grows`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 10)))
        assertThat(engine.state.value.progress).isWithin(1e-6f).of(0f)
        engine.tick()
        assertThat(engine.state.value.progress).isWithin(1e-6f).of(0.1f)
        engine.tick()
        assertThat(engine.state.value.progress).isWithin(1e-6f).of(0.2f)
    }

    @Test fun `full walkthrough finishes at total duration`() {
        val engine = WearPlayerEngine(listOf(stretch("a", 2), stretch("b", 3)))
        var finishedOnLast = false
        for (i in 0 until 5) finishedOnLast = engine.tick()
        assertThat(finishedOnLast).isTrue()
        assertThat(engine.state.value.finished).isTrue()
    }
}
