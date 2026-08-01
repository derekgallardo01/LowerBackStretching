package com.lowerbackstretching.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lowerbackstretching.App
import com.lowerbackstretching.data.ContentRepository
import com.lowerbackstretching.data.CustomRoutineRepository
import com.lowerbackstretching.data.FlexibilityRepository
import com.lowerbackstretching.data.PainLogRepository
import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.ProgramProgressRepository
import com.lowerbackstretching.data.SessionRepository
import com.lowerbackstretching.data.db.CustomRoutineEntity
import com.lowerbackstretching.data.db.FlexibilityTestEntity
import com.lowerbackstretching.data.db.PainLogEntity
import com.lowerbackstretching.data.db.SessionEntity
import com.lowerbackstretching.health.HealthController
import com.lowerbackstretching.sync.SyncController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Single shared ViewModel that exposes the app's repositories to every
 * Compose screen. Screens use `vm: AppViewModel = viewModel()` and read
 * whichever repositories they need.
 *
 * ## Why the stats below are properties, not repository calls
 *
 * The repository stat accessors (`sessions.streak()`, `flexibility.all()`, …)
 * are *functions* that build a fresh `Flow` on each call. `collectAsState`
 * keys its internal `remember` on the flow instance, so calling one directly
 * inside a composable produced a new flow on every recomposition — cancelling
 * collection and re-running the underlying Room query each time. `streak()` was
 * the worst case: it re-mapped every completed day and re-ran `computeStreak`
 * over the whole set on each pass.
 *
 * Hoisting them here as `stateIn`-backed [StateFlow]s gives every screen one
 * shared, stable instance that also survives configuration changes.
 * `WhileSubscribed(5_000)` keeps the upstream query alive across a rotation
 * without leaving it running indefinitely in the background.
 *
 * Screens should collect these with `collectAsStateWithLifecycle()`.
 */
class AppViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private val appCtx: App get() = getApplication()
    val content: ContentRepository get() = appCtx.contentRepository
    val sessions: SessionRepository get() = appCtx.sessionRepository
    val customRoutines: CustomRoutineRepository get() = appCtx.customRoutineRepository
    val programProgress: ProgramProgressRepository get() = appCtx.programProgressRepository
    val flexibility: FlexibilityRepository get() = appCtx.flexibilityRepository
    val painLog: PainLogRepository get() = appCtx.painLogRepository
    val health: HealthController get() = appCtx.health
    val sync: SyncController get() = appCtx.sync
    val prefs: Prefs get() = appCtx.prefs

    private fun <T> sharedState(
        flow: kotlinx.coroutines.flow.Flow<T>,
        initial: T,
    ): StateFlow<T> =
        flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = initial,
        )

    // ---------- Session stats ----------

    val streak: StateFlow<Int> = sharedState(sessions.streak(), 0)
    val longestStreak: StateFlow<Int> = sharedState(sessions.longestStreak(), 0)
    val sessionCount: StateFlow<Int> = sharedState(sessions.count(), 0)
    val totalDurationSeconds: StateFlow<Int> = sharedState(sessions.totalDurationSeconds(), 0)
    val completedDays: StateFlow<Set<LocalDate>> = sharedState(sessions.completedDays(), emptySet())
    val recentSessions: StateFlow<List<SessionEntity>> = sharedState(sessions.recent(), emptyList())

    // ---------- Other list-backed screens ----------

    val routines: StateFlow<List<CustomRoutineEntity>> =
        sharedState(customRoutines.all(), emptyList())
    val flexibilityHistory: StateFlow<List<FlexibilityTestEntity>> =
        sharedState(flexibility.all(), emptyList())
    val painHistory: StateFlow<List<PainLogEntity>> = sharedState(painLog.all(), emptyList())

    private companion object {
        /**
         * Keep upstream collection alive briefly after the last subscriber goes
         * away, so a rotation or tab switch doesn't tear down and re-run every
         * query. Long enough to cover a config change, short enough that a
         * backgrounded app stops querying.
         */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
