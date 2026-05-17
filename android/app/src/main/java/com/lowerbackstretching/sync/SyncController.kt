package com.lowerbackstretching.sync

import com.lowerbackstretching.data.Prefs
import kotlinx.coroutines.flow.Flow

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
}
