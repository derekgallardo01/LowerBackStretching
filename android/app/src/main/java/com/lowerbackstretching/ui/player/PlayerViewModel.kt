package com.lowerbackstretching.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lowerbackstretching.App
import com.lowerbackstretching.data.model.Stretch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives a [PlayerEngine] on a one-second tick and records a session to the
 * database when the routine finishes. Three `load*` entry points seed the
 * engine with different stretch sources.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx: App get() = getApplication()

    private val _engine = MutableStateFlow<PlayerEngine?>(null)

    val state: StateFlow<PlayerEngine.Snapshot?> = _engine
        .flatMapLatest { it?.state ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var programId: String = ""
    private var dayNumber: Int = 1
    private var loaded: Boolean = false

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loaded && this.programId == programId && this.dayNumber == dayNumber) return
        val program = appCtx.contentRepository.program(programId) ?: return
        this.programId = programId
        this.dayNumber = dayNumber
        initEngine(appCtx.contentRepository.stretchesFor(program, dayNumber))
    }

    fun loadSingle(stretchId: String) {
        val pid = "single-$stretchId"
        if (loaded && this.programId == pid) return
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        this.programId = pid
        this.dayNumber = 0
        initEngine(listOf(stretch))
    }

    fun loadCustomRoutine(routineId: Long) {
        val pid = "routine-$routineId"
        if (loaded && this.programId == pid) return
        viewModelScope.launch {
            val routine = appCtx.customRoutineRepository.byId(routineId) ?: return@launch
            val stretches = routine.stretchIds.mapNotNull { appCtx.contentRepository.stretch(it) }
            programId = pid
            dayNumber = 0
            initEngine(stretches)
        }
    }

    fun togglePlay() = _engine.value?.togglePlay()

    fun next() {
        val engine = _engine.value ?: return
        if (engine.next()) recordSession(engine.totalDurationSeconds)
    }

    fun previous() = _engine.value?.previous()

    private fun initEngine(stretches: List<Stretch>) {
        loaded = true
        val engine = PlayerEngine(stretches)
        _engine.value = engine
        startTicker(engine)
    }

    private fun startTicker(engine: PlayerEngine) {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (engine.tick()) recordSession(engine.totalDurationSeconds)
            }
        }
    }

    private fun recordSession(durationSeconds: Int) {
        viewModelScope.launch {
            appCtx.sessionRepository.recordCompletion(
                programId = programId,
                day = dayNumber,
                durationSeconds = durationSeconds,
            )
        }
    }
}
