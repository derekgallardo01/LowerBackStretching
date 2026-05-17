package com.lowerbackstretching.sync

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoopSyncBackendTest {

    private val backend = NoopSyncBackend()

    @Test fun `signedInUid returns null`() = runTest {
        assertThat(backend.signedInUid()).isNull()
    }

    @Test fun `signInAnonymously returns null`() = runTest {
        assertThat(backend.signInAnonymously()).isNull()
    }

    @Test fun `signOut completes without error`() = runTest {
        backend.signOut() // shouldn't throw
    }

    @Test fun `pushSession returns false`() = runTest {
        val ok = backend.pushSession(
            programId = "lower-back-relief-7day",
            dayNumber = 1,
            durationSeconds = 300,
            completedAtEpochMillis = 1_700_000_000_000,
            type = "program",
        )
        assertThat(ok).isFalse()
    }

    @Test fun `pushRoutine returns false`() = runTest {
        val ok = backend.pushRoutine(
            localId = 1L,
            name = "Morning",
            stretchIds = listOf("cat-cow"),
            displayOrder = 0,
            deletedAtEpochMillis = null,
        )
        assertThat(ok).isFalse()
    }

    @Test fun `pushProgramProgress returns false`() = runTest {
        assertThat(backend.pushProgramProgress("p1", 2, 0L)).isFalse()
    }

    @Test fun `pushFlexibilityTest returns false`() = runTest {
        val ok = backend.pushFlexibilityTest(
            recordedAtEpochMillis = 0L,
            sitAndReachCm = 10f,
            toeTouchCm = null,
            shoulderReachCm = null,
        )
        assertThat(ok).isFalse()
    }
}
