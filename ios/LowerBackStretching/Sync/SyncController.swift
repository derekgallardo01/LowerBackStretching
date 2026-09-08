import Foundation
import SwiftUI

/// App-wide façade that the UI talks to instead of `SyncBackend`
/// directly. Bundles the user-facing "is sync enabled" `@AppStorage`
/// pref with the pluggable backend so individual views don't have to
/// know about either.
///
/// Today the controller is mostly a thin pass-through; once the
/// Firebase backend lands it'll grow to include the outbox flush
/// worker and conflict-resolution hooks.
@MainActor
final class SyncController: ObservableObject {

    static let shared = SyncController()

    /// Swap to a real implementation (FirebaseSyncBackend, etc.) when
    /// ready. Setting this from outside the type is intentional —
    /// `LowerBackStretchingApp.init` is the right place.
    var backend: SyncBackend = NoopSyncBackend()

    private init() {}

    var backendType: String { String(describing: type(of: backend)) }
    var hasRealBackend: Bool { !(backend is NoopSyncBackend) }

    /// Current toggle state. Defaults ON: sync is anonymous session counts
    /// only, disclosed in the privacy policy, and Settings can turn it off.
    var isEnabled: Bool {
        UserDefaults.standard.object(forKey: SettingsKeys.cloudSyncEnabled) == nil
            ? true
            : UserDefaults.standard.bool(forKey: SettingsKeys.cloudSyncEnabled)
    }

    /// Push one completed session, respecting the toggle. Signs in anonymously
    /// on first use. Best-effort — failures are dropped; SwiftData stays the
    /// source of truth.
    func pushSessionIfEnabled(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Int64,
        type: String
    ) async {
        guard hasRealBackend, isEnabled else { return }
        if await backend.signedInUid() == nil {
            _ = await backend.signInAnonymously()
        }
        _ = await backend.pushSession(
            programId: programId,
            dayNumber: dayNumber,
            durationSeconds: durationSeconds,
            completedAtEpochMillis: completedAtEpochMillis,
            type: type
        )
    }

    /// Mirror of `SettingsKeys.cloudSyncEnabled`. Wraps the
    /// sign-in / sign-out side effects.
    func setEnabled(_ enabled: Bool) async {
        UserDefaults.standard.set(enabled, forKey: SettingsKeys.cloudSyncEnabled)
        if enabled {
            // Eagerly sign in anonymously so the rest of the app can
            // assume a UID exists once the toggle is on.
            if await backend.signedInUid() == nil {
                _ = await backend.signInAnonymously()
            }
        } else {
            await backend.signOut()
        }
    }
}
