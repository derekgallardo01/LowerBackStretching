package com.lowerbackstretching.sync

/**
 * Default backend wired into [App.syncBackend] until a real one
 * (Firebase, Supabase, etc.) is installed. Every entry point returns
 * a "not signed in / didn't sync" answer; the rest of the app treats
 * sync as opt-in anyway, so this is a safe no-op.
 */
class NoopSyncBackend : SyncBackend {
    override suspend fun signedInUid(): String? = null
    override suspend fun signInAnonymously(): String? = null
    override suspend fun signOut() = Unit

    override suspend fun pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Long,
        type: String,
    ): Boolean = false

    override suspend fun pushRoutine(
        localId: Long,
        name: String,
        stretchIds: List<String>,
        displayOrder: Int,
        deletedAtEpochMillis: Long?,
    ): Boolean = false

    override suspend fun pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Long,
    ): Boolean = false

    override suspend fun pushFlexibilityTest(
        recordedAtEpochMillis: Long,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?,
    ): Boolean = false
}
