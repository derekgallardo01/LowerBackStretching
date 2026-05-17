package com.lowerbackstretching.wear.player

import com.lowerbackstretching.wear.model.WatchStretch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pure state machine for the watch-side stretch player. Mirrors the
 * phone-side [com.lowerbackstretching.ui.player.PlayerEngine] line-for-
 * line; consider extracting both into a shared :core module if the
 * duplication starts to drift.
 */
class WearPlayerEngine(
    initialStretches: List<WatchStretch>,
) {

    data class Snapshot(
        val stretches: List<WatchStretch>,
        val index: Int,
        val remainingSeconds: Int,
        val running: Boolean,
        val finished: Boolean,
    ) {
        val current: WatchStretch? get() = stretches.getOrNull(index)

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

    private val _finishedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val finishedEvents: SharedFlow<Unit> = _finishedEvents.asSharedFlow()

    /** Returns true if this tick caused the routine to finish. */
    fun tick(): Boolean {
        val s = _state.value
        if (!s.running || s.finished) return false
        return if (s.remainingSeconds > 1) {
            _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            false
        } else advance()
    }

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
            _finishedEvents.tryEmit(Unit)
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
