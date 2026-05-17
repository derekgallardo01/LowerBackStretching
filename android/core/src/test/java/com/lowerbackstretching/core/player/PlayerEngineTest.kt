package com.lowerbackstretching.core.player

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.model.Stretch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlayerEngineTest {

    private fun stretch(id: String, seconds: Int) = Stretch(
        id = id,
        name = id,
        bodyParts = listOf("lower-back"),
        durationSeconds = seconds,
        difficulty = "easy",
        description = "",
        youtubeId = "x",
    )

    @Test
    fun initial_state_loads_first_stretch() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        val s = engine.state.value
        assertThat(s.index).isEqualTo(0)
        assertThat(s.remainingSeconds).isEqualTo(10)
        assertThat(s.running).isTrue()
        assertThat(s.finished).isFalse()
        assertThat(s.current?.id).isEqualTo("a")
    }

    @Test
    fun empty_list_marks_finished() {
        val engine = PlayerEngine(emptyList())
        val s = engine.state.value
        assertThat(s.finished).isTrue()
        assertThat(s.current).isNull()
    }

    @Test
    fun tick_decrements_remaining_each_call() {
        val engine = PlayerEngine(listOf(stretch("a", 3)))
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(2)
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(1)
    }

    @Test
    fun tick_when_remaining_is_one_advances_to_next() {
        val engine = PlayerEngine(listOf(stretch("a", 1), stretch("b", 5)))
        val finished = engine.tick()
        assertThat(finished).isFalse()
        val s = engine.state.value
        assertThat(s.index).isEqualTo(1)
        assertThat(s.remainingSeconds).isEqualTo(5)
        assertThat(s.current?.id).isEqualTo("b")
    }

    @Test
    fun tick_after_last_stretch_marks_finished_and_returns_true() {
        val engine = PlayerEngine(listOf(stretch("a", 1)))
        val finished = engine.tick()
        assertThat(finished).isTrue()
        assertThat(engine.state.value.finished).isTrue()
        assertThat(engine.state.value.running).isFalse()
    }

    @Test
    fun tick_when_paused_is_a_noop() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        engine.togglePlay() // pause
        engine.tick()
        engine.tick()
        engine.tick()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test
    fun next_skips_to_following_stretch() {
        val engine = PlayerEngine(listOf(stretch("a", 30), stretch("b", 45)))
        engine.next()
        assertThat(engine.state.value.index).isEqualTo(1)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(45)
    }

    @Test
    fun next_on_last_stretch_marks_finished_and_returns_true() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        val finished = engine.next()
        assertThat(finished).isTrue()
        assertThat(engine.state.value.finished).isTrue()
    }

    @Test
    fun previous_at_index_zero_stays_at_zero() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        engine.previous()
        assertThat(engine.state.value.index).isEqualTo(0)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test
    fun previous_resets_remaining_to_full_duration() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        engine.next() // now at index 1, 20s
        engine.tick() // 19s remaining
        engine.previous() // back to index 0
        assertThat(engine.state.value.index).isEqualTo(0)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
    }

    @Test
    fun previous_unfinishes_when_called_after_finishing() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        engine.next() // finishes
        engine.previous()
        assertThat(engine.state.value.finished).isFalse()
        assertThat(engine.state.value.running).isTrue()
    }

    @Test
    fun togglePlay_flips_running() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        assertThat(engine.state.value.running).isTrue()
        engine.togglePlay()
        assertThat(engine.state.value.running).isFalse()
        engine.togglePlay()
        assertThat(engine.state.value.running).isTrue()
    }

    @Test
    fun progress_is_zero_at_start_and_grows() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        assertThat(engine.state.value.progress).isEqualTo(0f)
        engine.tick() // 9
        assertThat(engine.state.value.progress).isWithin(1e-6f).of(0.1f)
        engine.tick() // 8
        assertThat(engine.state.value.progress).isWithin(1e-6f).of(0.2f)
    }

    @Test
    fun progress_handles_zero_duration_stretch() {
        val engine = PlayerEngine(listOf(stretch("a", 0)))
        assertThat(engine.state.value.progress).isEqualTo(0f)
    }

    @Test
    fun totalDurationSeconds_sums_input() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 20), stretch("c", 30)))
        assertThat(engine.totalDurationSeconds).isEqualTo(60)
    }

    @Test
    fun next_after_finished_is_noop() {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        engine.next() // finishes
        val before = engine.state.value
        val again = engine.next()
        assertThat(again).isFalse()
        assertThat(engine.state.value).isEqualTo(before)
    }

    @Test
    fun full_routine_walkthrough_ends_finished_at_total_duration() {
        val engine = PlayerEngine(listOf(stretch("a", 2), stretch("b", 3)))
        // 5 ticks should finish exactly.
        var finishedOnLast = false
        repeat(5) { finishedOnLast = engine.tick() }
        assertThat(finishedOnLast).isTrue()
        assertThat(engine.state.value.finished).isTrue()
    }

    @Test
    fun finishedEvents_emits_once_with_total_duration_via_tick() = runTest {
        val engine = PlayerEngine(listOf(stretch("a", 2), stretch("b", 3)))
        // Subscribe BEFORE emitting — the engine's MutableSharedFlow is
        // replay=0, so emissions that happen with no active collector
        // are dropped. Production wiring (the ViewModel) starts the
        // collector before the ticker; the test has to mirror that.
        val deferred = async { engine.finishedEvents.first() }
        runCurrent()
        repeat(5) { engine.tick() }
        val event = deferred.await()
        assertThat(event.totalDurationSeconds).isEqualTo(5)
    }

    @Test
    fun finishedEvents_emits_via_next() = runTest {
        val engine = PlayerEngine(listOf(stretch("a", 10)))
        val deferred = async { engine.finishedEvents.first() }
        runCurrent()
        engine.next()
        val event = deferred.await()
        assertThat(event.totalDurationSeconds).isEqualTo(10)
    }

    @Test
    fun startIndex_seeks_to_given_stretch() {
        val engine = PlayerEngine(
            initialStretches = listOf(stretch("a", 10), stretch("b", 20), stretch("c", 30)),
            startIndex = 1,
        )
        val s = engine.state.value
        assertThat(s.index).isEqualTo(1)
        assertThat(s.remainingSeconds).isEqualTo(20)
        assertThat(s.current?.id).isEqualTo("b")
    }

    @Test
    fun startIndex_negative_is_clamped_to_zero() {
        val engine = PlayerEngine(listOf(stretch("a", 10)), startIndex = -3)
        assertThat(engine.state.value.index).isEqualTo(0)
    }

    @Test
    fun startIndex_past_end_is_clamped() {
        val engine = PlayerEngine(
            initialStretches = listOf(stretch("a", 10), stretch("b", 20)),
            startIndex = 99,
        )
        assertThat(engine.state.value.index).isEqualTo(1)
    }

    @Test
    fun routineProgress_is_zero_at_first_frame() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 20)))
        assertThat(engine.state.value.routineProgress).isWithin(1e-6f).of(0f)
    }

    @Test
    fun routineProgress_advances_proportionally_within_a_stretch() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 30)))
        engine.tick() // 1s of 40 total
        assertThat(engine.state.value.routineProgress).isWithin(1e-6f).of(1f / 40f)
    }

    @Test
    fun routineProgress_jumps_when_a_stretch_completes() {
        val engine = PlayerEngine(listOf(stretch("a", 10), stretch("b", 30)))
        for (i in 0 until 10) engine.tick() // finish first stretch
        // We're now at the start of stretch b: 10 of 40 seconds elapsed.
        assertThat(engine.state.value.routineProgress).isWithin(1e-6f).of(0.25f)
    }

    @Test
    fun routineProgress_is_one_when_finished() {
        val engine = PlayerEngine(listOf(stretch("a", 2)))
        for (i in 0 until 2) engine.tick()
        assertThat(engine.state.value.finished).isTrue()
        assertThat(engine.state.value.routineProgress).isEqualTo(1f)
    }

    @Test
    fun routineProgress_zero_for_zero_duration_stretches() {
        val engine = PlayerEngine(listOf(stretch("a", 0), stretch("b", 0)))
        assertThat(engine.state.value.routineProgress).isEqualTo(0f)
    }
}
