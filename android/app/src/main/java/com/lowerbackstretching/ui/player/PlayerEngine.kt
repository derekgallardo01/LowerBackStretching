package com.lowerbackstretching.ui.player

import com.lowerbackstretching.data.model.Stretch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pure state machine for the stretch player — no Android or coroutine
 * dependencies, so it's directly unit-testable. The viewmodel drives
 * [tick] on a one-second cadence and listens for [Snapshot.finished].
 */
class PlayerEngine(initialStretches: List<Stretch>) {

    data class Snapshot(
        val stretches: List<Stretch>,
        val index: Int,
        val remainingSeconds: Int,
        val running: Boolean,
        val finished: Boolean,
    ) {
        val current: Stretch? get() = stretches.getOrNull(index)

        val progress: Float
            get() = current?.let {
                if (it.durationSeconds == 0) 0f
                else (it.durationSeconds - remainingSeconds).toFloat() / it.durationSeconds
            } ?: 0f
    }

    private val _state = MutableStateFlow(
        Snapshot(
            stretches = initialStretches,
            index = 0,
            remainingSeconds = initialStretches.firstOrNull()?.durationSeconds ?: 0,
            running = initialStretches.isNotEmpty(),
            finished = initialStretches.isEmpty(),
        )
    )
    val state: StateFlow<Snapshot> = _state.asStateFlow()

    val totalDurationSeconds: Int = initialStretches.sumOf { it.durationSeconds }

    /**
     * Advance the clock by one second. Returns true if this tick caused the
     * routine to finish, so the caller can record the session.
     */
    fun tick(): Boolean {
        val s = _state.value
        if (!s.running || s.finished) return false
        return if (s.remainingSeconds > 1) {
            _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            false
        } else {
            advance()
        }
    }

    /** Skip to the next stretch immediately. Returns true if finished. */
    fun next(): Boolean {
        if (_state.value.finished) return false
        return advance()
    }

    fun previous() {
        val s = _state.value
        val prev = (s.index - 1).coerceAtLeast(0)
        _state.update {
            it.copy(
                index = prev,
                remainingSeconds = it.stretches[prev].durationSeconds,
                finished = false,
                running = true,
            )
        }
    }

    fun togglePlay() = _state.update { it.copy(running = !it.running) }

    private fun advance(): Boolean {
        val s = _state.value
        val nextIdx = s.index + 1
        return if (nextIdx >= s.stretches.size) {
            _state.update { it.copy(finished = true, running = false) }
            true
        } else {
            _state.update {
                it.copy(
                    index = nextIdx,
                    remainingSeconds = it.stretches[nextIdx].durationSeconds,
                )
            }
            false
        }
    }
}
