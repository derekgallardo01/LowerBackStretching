import Foundation

/// The cross-cutting protocol every cloud-sync implementation conforms
/// to. The default wired into `LowerBackStretchingApp` is
/// `NoopSyncBackend`; the Firebase implementation will land in a
/// follow-up once the iOS config (`GoogleService-Info.plist`) is in
/// the project.
///
/// Every method is `async` so backends can do network I/O without
/// blocking. Return types are optional / Bool so the call site can
/// branch on success without catching errors.
protocol SyncBackend {

    /// UID if the user is already signed in, else nil.
    func signedInUid() async -> String?

    /// Sign in anonymously and return the resulting UID. Backends that
    /// support upgrade-to-email/Google later still start here so the
    /// user can use sync immediately without picking a provider.
    func signInAnonymously() async -> String?

    /// Sign out, abandoning the anonymous session if any.
    func signOut() async

    /// Push a single completed session up to the cloud. Returns true
    /// on success. Idempotency is the backend's responsibility — the
    /// outbox doesn't dedupe.
    func pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Int64,
        type: String
    ) async -> Bool

    /// Upsert a custom routine.
    func pushRoutine(
        localId: String,
        name: String,
        stretchIds: [String],
        displayOrder: Int,
        deletedAtEpochMillis: Int64?
    ) async -> Bool

    /// Upsert a per-program day bookmark.
    func pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Int64
    ) async -> Bool

    /// Append a flexibility-test measurement.
    func pushFlexibilityTest(
        recordedAtEpochMillis: Int64,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?
    ) async -> Bool
}
