package com.lowerbackstretching.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lowerbackstretching.App
import com.lowerbackstretching.audio.AudioController
import com.lowerbackstretching.core.Achievement
import com.lowerbackstretching.core.InProgressSession
import com.lowerbackstretching.core.SyntheticProgramId
import com.lowerbackstretching.core.evaluateAchievements
import com.lowerbackstretching.core.levelFor
import com.lowerbackstretching.core.model.Stretch
import com.lowerbackstretching.core.player.PlayerEngine
import com.lowerbackstretching.core.xpForSession
import com.lowerbackstretching.notifications.Haptics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

sealed class PainPromptState {
    data object Hidden : PainPromptState()
    data object PreSession : PainPromptState()
    data class PostSession(val sessionId: Long) : PainPromptState()
}

/**
 * Snapshot of stat deltas captured at session completion, used by the
 * FinishedView to render reinforcement callouts (streak count-up, level
 * pop, pain delta). Null when no session has finished yet in the
 * lifetime of this ViewModel.
 */
data class FinishedSessionState(
    val sessionId: Long,
    val streakBefore: Int,
    val streakAfter: Int,
    val levelBefore: Int,
    val levelAfter: Int,
    val newlyUnlocked: List<Achievement> = emptyList(),
) {
    val streakIncreased: Boolean get() = streakAfter > streakBefore
    val leveledUp: Boolean get() = levelAfter > levelBefore
}

/** Streak thresholds that earn a milestone modal. First crossing only. */
internal val MILESTONE_THRESHOLDS = listOf(7, 30, 100, 365)

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

    private val _engine = MutableStateFlow<PlayerEngine<Stretch>?>(null)

    val state: StateFlow<PlayerEngine.Snapshot<Stretch>?> = _engine
        .flatMapLatest { it?.state ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var programId: String = ""
    private var dayNumber: Int = 1
    private var loaded: Boolean = false
    private var loadRequested: Boolean = false
    private var pendingLoad: (suspend () -> Unit)? = null
    private var tickerJob: Job? = null
    private var finishedJob: Job? = null
    private var transitionJob: Job? = null
    private var audioJob: Job? = null

    private val _painPrompt = MutableStateFlow<PainPromptState>(PainPromptState.Hidden)
    val painPrompt: StateFlow<PainPromptState> = _painPrompt.asStateFlow()

    private val _finishedSession = MutableStateFlow<FinishedSessionState?>(null)
    val finishedSession: StateFlow<FinishedSessionState?> = _finishedSession.asStateFlow()

    private val _milestone = MutableStateFlow<Int?>(null)
    val milestone: StateFlow<Int?> = _milestone.asStateFlow()

    fun dismissMilestone() {
        _milestone.value = null
    }

    fun loadProgram(programId: String, dayNumber: Int) {
        if (loadRequested && this.programId == programId && this.dayNumber == dayNumber) return
        val program = appCtx.contentRepository.program(programId) ?: return
        this.programId = programId
        this.dayNumber = dayNumber
        loadRequested = true
        pendingLoad = {
            initEngine(
                stretches = appCtx.contentRepository.stretchesFor(program, dayNumber),
                startIndex = resumeIndexFor(programId, dayNumber),
            )
        }
        viewModelScope.launch { maybeStartOrPromptPre() }
    }

    fun loadSingle(stretchId: String) {
        val pid = SyntheticProgramId.single(stretchId)
        if (loadRequested && this.programId == pid) return
        val stretch = appCtx.contentRepository.stretch(stretchId) ?: return
        this.programId = pid
        this.dayNumber = 0
        loadRequested = true
        pendingLoad = {
            initEngine(
                stretches = listOf(stretch),
                startIndex = resumeIndexFor(pid, 0),
            )
        }
        viewModelScope.launch { maybeStartOrPromptPre() }
    }

    fun loadCustomRoutine(routineId: Long) {
        val pid = SyntheticProgramId.routine(routineId)
        if (loadRequested && this.programId == pid) return
        this.programId = pid
        this.dayNumber = 0
        loadRequested = true
        pendingLoad = {
            val routine = appCtx.customRoutineRepository.byId(routineId)
            if (routine != null) {
                val stretches = routine.stretchIds.mapNotNull {
                    appCtx.contentRepository.stretch(it)
                }
                initEngine(
                    stretches = stretches,
                    startIndex = resumeIndexFor(pid, 0),
                )
            }
        }
        viewModelScope.launch { maybeStartOrPromptPre() }
    }

    /** Either runs the pending load now or shows the pre-session prompt first. */
    private suspend fun maybeStartOrPromptPre() {
        if (appCtx.painLogRepository.hasPreLoggedToday()) {
            runPendingLoad()
        } else {
            _painPrompt.value = PainPromptState.PreSession
        }
    }

    private suspend fun runPendingLoad() {
        val load = pendingLoad ?: return
        pendingLoad = null
        load()
    }

    fun onPrePromptSubmit(level: Int, bodyLocationTag: String?) {
        viewModelScope.launch {
            appCtx.painLogRepository.recordPre(level, bodyLocationTag)
            _painPrompt.value = PainPromptState.Hidden
            runPendingLoad()
        }
    }

    fun onPrePromptSkip() {
        viewModelScope.launch {
            _painPrompt.value = PainPromptState.Hidden
            runPendingLoad()
        }
    }

    fun onPostPromptSubmit(sessionId: Long, level: Int, bodyLocationTag: String?) {
        viewModelScope.launch {
            appCtx.painLogRepository.recordPost(level, bodyLocationTag, sessionId)
            _painPrompt.value = PainPromptState.Hidden
        }
    }

    fun onPostPromptSkip() {
        _painPrompt.value = PainPromptState.Hidden
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
                // Capture before-state for the FinishedView reinforcement callouts.
                val totalSecondsBefore = appCtx.sessionRepository.totalDurationSeconds().first()
                val streakBefore = appCtx.sessionRepository.streak().first()
                val sessionsBefore = appCtx.sessionRepository.count().first()
                val longestStreakBefore = appCtx.sessionRepository.longestStreak().first()
                val levelBefore = levelFor(xpForSession(totalSecondsBefore))
                val achievementsBefore = evaluateAchievements(
                    totalSessions = sessionsBefore,
                    longestStreak = longestStreakBefore,
                    level = levelBefore,
                )

                val sessionId = appCtx.sessionRepository.recordCompletion(
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
                appCtx.prefs.setLastSessionEpochDay(LocalDate.now().toEpochDay())
                if (appCtx.prefs.healthWriteEnabled.first()) {
                    val end = Instant.now()
                    val start = end.minusSeconds(event.totalDurationSeconds.toLong())
                    appCtx.health.writeStretchingSession(start = start, end = end)
                }
                appCtx.prefs.clearInProgress()
                if (appCtx.prefs.hapticsFinish.first()) Haptics.finish(appCtx)

                // Capture after-state and publish for the FinishedView.
                val totalSecondsAfter = appCtx.sessionRepository.totalDurationSeconds().first()
                val streakAfter = appCtx.sessionRepository.streak().first()
                val sessionsAfter = appCtx.sessionRepository.count().first()
                val longestStreakAfter = appCtx.sessionRepository.longestStreak().first()
                val levelAfter = levelFor(xpForSession(totalSecondsAfter))
                val achievementsAfter = evaluateAchievements(
                    totalSessions = sessionsAfter,
                    longestStreak = longestStreakAfter,
                    level = levelAfter,
                )

                val previouslyLocked = achievementsBefore
                    .filter { !it.unlocked }
                    .map { it.achievement.id }
                    .toSet()
                val newlyUnlocked = achievementsAfter
                    .filter { it.unlocked && it.achievement.id in previouslyLocked }
                    .map { it.achievement }

                _finishedSession.value = FinishedSessionState(
                    sessionId = sessionId,
                    streakBefore = streakBefore,
                    streakAfter = streakAfter,
                    levelBefore = levelBefore,
                    levelAfter = levelAfter,
                    newlyUnlocked = newlyUnlocked,
                )

                // Milestone modal: fire on the first crossing of 7/30/100/365.
                val shown = appCtx.prefs.milestonesShown.first()
                val crossed = MILESTONE_THRESHOLDS.firstOrNull { t ->
                    streakBefore < t && streakAfter >= t && t !in shown
                }
                if (crossed != null) {
                    appCtx.prefs.markMilestoneShown(crossed)
                    _milestone.value = crossed
                }

                _painPrompt.value = PainPromptState.PostSession(sessionId)
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
        val music: com.lowerbackstretching.core.MusicTrack,
        val musicVolume: Float,
        val ambient: com.lowerbackstretching.core.AmbientTrack,
        val ambientVolume: Float,
    )
}
