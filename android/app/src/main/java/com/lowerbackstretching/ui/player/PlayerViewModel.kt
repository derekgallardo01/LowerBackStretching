package com.lowerbackstretching.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lowerbackstretching.App
import com.lowerbackstretching.data.SyntheticProgramId
import com.lowerbackstretching.data.model.Stretch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives a [PlayerEngine] on a one-second tick and records a session
 * to the database when the engine emits its [PlayerEngine.FinishedEvent].
 * Three `load*` entry points seed the engine with different stretch
 * sources; recording always goes through the same Flow listener so the
 * trigger can't drift between sources.
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
    private var tickerJob: Job? = null
    private var finishedJob: Job? = null

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loaded && this.programId == programId && this.dayNumber == dayNumber) return
        val program = appCtx.contentRepository.program(programId) ?: return
        this.programId = programId
        this.dayNumber = dayNumber
        initEngine(appCtx.contentRepository.stretchesFor(program, dayNumber))
    }

    fun loadSingle(stretchId: String) {
        val pid = SyntheticProgramId.single(stretchId)
        if (loaded && this.programId == pid) return
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        this.programId = pid
        this.dayNumber = 0
        initEngine(listOf(stretch))
    }

    fun loadCustomRoutine(routineId: Long) {
        val pid = SyntheticProgramId.routine(routineId)
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
        _engine.value?.next()
    }

    fun previous() = _engine.value?.previous()

    private fun initEngine(stretches: List<Stretch>) {
        loaded = true
        tickerJob?.cancel()
        finishedJob?.cancel()
        val engine = PlayerEngine(stretches)
        _engine.value = engine
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                engine.tick()
            }
        }
        finishedJob = viewModelScope.launch {
            engine.finishedEvents.collect { event ->
                appCtx.sessionRepository.recordCompletion(
                    programId = programId,
                    day = dayNumber,
                    durationSeconds = event.totalDurationSeconds,
                )
            }
        }
    }
}
