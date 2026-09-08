package com.lowerbackstretching.sync

import com.lowerbackstretching.data.Prefs
import com.lowerbackstretching.data.db.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * App-wide façade that the UI talks to instead of the [SyncBackend]
 * directly. Bundles the user-facing "is sync enabled" pref with the
 * pluggable backend so individual screens don't have to know about
 * either.
 *
 * Today the controller is mostly a thin pass-through; once the
 * Firebase backend lands it'll grow to include the outbox flush
 * worker and conflict-resolution hooks.
 */
class SyncController(
    private val backend: SyncBackend,
    private val prefs: Prefs,
) {
    /** Active backend so screens can render its status. */
    val backendType: String = backend::class.simpleName ?: "Unknown"

    /** "Enable cloud sync" toggle from Settings. */
    val enabled: Flow<Boolean> = prefs.cloudSyncEnabled

    suspend fun setEnabled(value: Boolean) {
        prefs.setCloudSyncEnabled(value)
        if (value) {
            // Eagerly sign in anonymously so the rest of the app can
            // assume a UID exists once the toggle is on.
            if (backend.signedInUid() == null) backend.signInAnonymously()
        } else {
            backend.signOut()
        }
    }

    /** True when there's a backend that can actually push. */
    val hasRealBackend: Boolean = backend !is NoopSyncBackend

    /**
     * Push one completed session, respecting the user's sync toggle. Signs in
     * anonymously on first use. Best-effort: any failure is silently dropped —
     * Room stays the source of truth and the player flow never blocks on this.
     */
    suspend fun pushSessionIfEnabled(session: SessionEntity) {
        if (!hasRealBackend || !enabled.first()) return
        backend.pushSession(
            programId = session.programId,
            dayNumber = session.dayNumber,
            durationSeconds = session.durationSeconds,
            completedAtEpochMillis = session.completedAtEpochMillis,
            type = session.type,
        )
    }
}
