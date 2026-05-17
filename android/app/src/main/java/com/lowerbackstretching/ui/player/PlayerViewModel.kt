package com.lowerbackstretching.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lowerbackstretching.App
import com.lowerbackstretching.audio.AudioController
import com.lowerbackstretching.data.InProgressSession
import com.lowerbackstretching.data.SyntheticProgramId
import com.lowerbackstretching.data.model.Stretch
import com.lowerbackstretching.notifications.Haptics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
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
    private var transitionJob: Job? = null
    private var audioJob: Job? = null

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loaded && this.programId == programId && this.dayNumber == dayNumber) return
        val program = appCtx.contentRepository.program(programId) ?: return
        this.programId = programId
        this.dayNumber = dayNumber
        viewModelScope.launch {
            initEngine(
                stretches = appCtx.contentRepository.stretchesFor(program, dayNumber),
                startIndex = resumeIndexFor(programId, dayNumber),
            )
        }
    }

    fun loadSingle(stretchId: String) {
        val pid = SyntheticProgramId.single(stretchId)
        if (loaded && this.programId == pid) return
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        this.programId = pid
        this.dayNumber = 0
        viewModelScope.launch {
            initEngine(
                stretches = listOf(stretch),
                startIndex = resumeIndexFor(pid, 0),
            )
        }
    }

    fun loadCustomRoutine(routineId: Long) {
        val pid = SyntheticProgramId.routine(routineId)
        if (loaded && this.programId == pid) return
        viewModelScope.launch {
            val routine = appCtx.customRoutineRepository.byId(routineId) ?: return@launch
            val stretches = routine.stretchIds.mapNotNull { appCtx.contentRepository.stretch(it) }
            programId = pid
            dayNumber = 0
            initEngine(
                stretches = stretches,
                startIndex = resumeIndexFor(pid, 0),
            )
        }
    }

    /** Returns the saved index if it matches this source, else 0. */
    private suspend fun resumeIndexFor(programId: String, dayNumber: Int): Int {
        val saved = appCtx.prefs.inProgressSession.first() ?: return 0
        return if (saved.programId == programId && saved.dayNumber == dayNumber) saved.index else 0
    }

    fun togglePlay() = _engine.value?.togglePlay()

    fun next() {
        _engine.value?.next()
    }

    fun previous() = _engine.value?.previous()

    /** Tear down audio and cancel running jobs. Call from the view's onDispose. */
    fun stop() {
        tickerJob?.cancel(); tickerJob = null
        finishedJob?.cancel(); finishedJob = null
        transitionJob?.cancel(); transitionJob = null
        audioJob?.cancel(); audioJob = null
        AudioController.stopAll()
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }

    private fun initEngine(stretches: List<Stretch>, startIndex: Int = 0) {
        loaded = true
        tickerJob?.cancel()
        finishedJob?.cancel()
        transitionJob?.cancel()
        audioJob?.cancel()
        val engine = PlayerEngine(stretches, startIndex = startIndex)
        _engine.value = engine
        // Persist the resume point on first frame (even before any tick)
        // so a force-kill on the very first stretch still resumes here.
        viewModelScope.launch {
            appCtx.prefs.saveInProgress(
                InProgressSession(programId, dayNumber, engine.state.value.index)
            )
        }
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
                appCtx.contentRepository.program(programId)?.let { program ->
                    appCtx.programProgressRepository.advance(
                        programId = programId,
                        completedDay = dayNumber,
                        totalDays = program.days.size,
                    )
                }
                appCtx.prefs.clearInProgress()
                if (appCtx.prefs.hapticsFinish.first()) Haptics.finish(appCtx)
            }
        }
        transitionJob = viewModelScope.launch {
            engine.state
                .distinctUntilChangedBy { it.index }
                .drop(1)
                .collect { snapshot ->
                    if (!snapshot.finished) {
                        appCtx.prefs.saveInProgress(
                            InProgressSession(programId, dayNumber, snapshot.index)
                        )
                        if (appCtx.prefs.hapticsTransitions.first()) {
                            Haptics.short(appCtx)
                        }
                        val chime = appCtx.prefs.chimeTrack.first()
                        AudioController.playChime(appCtx, chime)
                    }
                }
        }
        audioJob = viewModelScope.launch {
            combine(
                appCtx.prefs.musicTrack,
                appCtx.prefs.musicVolume,
                appCtx.prefs.ambientTrack,
                appCtx.prefs.ambientVolume,
            ) { music, mv, ambient, av -> AudioState(music, mv, ambient, av) }
                .collect { s ->
                    AudioController.setMusic(appCtx, s.music, s.musicVolume)
                    AudioController.setAmbient(appCtx, s.ambient, s.ambientVolume)
                }
        }
    }

    private data class AudioState(
        val music: com.lowerbackstretching.audio.MusicTrack,
        val musicVolume: Float,
        val ambient: com.lowerbackstretching.audio.AmbientTrack,
        val ambientVolume: Float,
    )
}
