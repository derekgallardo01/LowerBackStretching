package com.lowerbackstretching.sync

/**
 * The cross-cutting interface every cloud-sync implementation
 * implements. The default in [App] is [NoopSyncBackend]; the Firebase
 * implementation will land in a follow-up once the FE config files
 * (`google-services.json`) are wired into the project.
 *
 * Every method is suspend so backends can do network I/O without
 * blocking. Return types are nullable / boolean so the call site can
 * branch on success without parsing exceptions.
 */
interface SyncBackend {

    /** UID if the user is already signed in, else null. */
    suspend fun signedInUid(): String?

    /**
     * Sign in anonymously and return the resulting UID. Backends that
     * support upgrade-to-email/Google later still start here so the
     * user can use sync immediately without picking a provider.
     */
    suspend fun signInAnonymously(): String?

    /** Sign out, abandoning the anonymous session if any. */
    suspend fun signOut()

    /**
     * Push a single completed session up to the cloud. Returns true
     * on success. Idempotency is the backend's responsibility — the
     * outbox doesn't dedupe.
     */
    suspend fun pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Long,
        type: String,
    ): Boolean

    /** Upsert a custom routine. */
    suspend fun pushRoutine(
        localId: Long,
        name: String,
        stretchIds: List<String>,
        displayOrder: Int,
        deletedAtEpochMillis: Long?,
    ): Boolean

    /** Upsert a per-program day bookmark. */
    suspend fun pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Long,
    ): Boolean

    /** Append a flexibility-test measurement. */
    suspend fun pushFlexibilityTest(
        recordedAtEpochMillis: Long,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?,
    ): Boolean
}
