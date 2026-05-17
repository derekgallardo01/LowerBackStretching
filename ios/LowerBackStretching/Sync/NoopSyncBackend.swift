import Foundation

/// Default backend used until a real one (Firebase, Supabase, etc.)
/// is installed. Every entry point returns a "not signed in / didn't
/// sync" answer; the rest of the app treats sync as opt-in anyway, so
/// this is a safe no-op.
struct NoopSyncBackend: SyncBackend {

    func signedInUid() async -> String? { nil }
    func signInAnonymously() async -> String? { nil }
    func signOut() async {}

    func pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Int64,
        type: String
    ) async -> Bool { false }

    func pushRoutine(
        localId: String,
        name: String,
        stretchIds: [String],
        displayOrder: Int,
        deletedAtEpochMillis: Int64?
    ) async -> Bool { false }

    func pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Int64
    ) async -> Bool { false }

    func pushFlexibilityTest(
        recordedAtEpochMillis: Int64,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?
    ) async -> Bool { false }
}
