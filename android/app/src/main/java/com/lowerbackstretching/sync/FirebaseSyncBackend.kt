package com.lowerbackstretching.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Real [SyncBackend] against Firebase: anonymous auth + Firestore writes to the
 * per-user tree the rules in `firebase/firestore.rules` were written for
 * (`/users/{uid}/sessions|routines|programProgress|flexibilityTests`).
 *
 * Document ids are deterministic (timestamps / local ids), so retried pushes
 * are idempotent upserts rather than duplicates. Every method is best-effort:
 * failures return false/null and never throw into callers — sessions stay
 * canonical in Room, the cloud copy is a mirror.
 */
class FirebaseSyncBackend : SyncBackend {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    override suspend fun signedInUid(): String? = auth.currentUser?.uid

    override suspend fun signInAnonymously(): String? = try {
        auth.signInAnonymously().await().user?.uid
    } catch (_: Exception) {
        null
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            // Ignore — signing out a stale session is best-effort.
        }
    }

    private suspend fun set(collection: String, docId: String, data: Map<String, Any?>): Boolean {
        val uid = signedInUid() ?: signInAnonymously() ?: return false
        return try {
            db.collection("users").document(uid).collection(collection)
                .document(docId)
                .set(data.filterValues { it != null }, SetOptions.merge())
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Long,
        type: String,
    ): Boolean = set(
        collection = "sessions",
        docId = completedAtEpochMillis.toString(),
        data = mapOf(
            "programId" to programId,
            "dayNumber" to dayNumber,
            "durationSeconds" to durationSeconds,
            "completedAtEpochMillis" to completedAtEpochMillis,
            "type" to type,
            "platform" to "android",
        ),
    )

    override suspend fun pushRoutine(
        localId: Long,
        name: String,
        stretchIds: List<String>,
        displayOrder: Int,
        deletedAtEpochMillis: Long?,
    ): Boolean = set(
        collection = "routines",
        docId = localId.toString(),
        data = mapOf(
            "name" to name,
            "stretchIds" to stretchIds,
            "displayOrder" to displayOrder,
            "deletedAtEpochMillis" to deletedAtEpochMillis,
            "platform" to "android",
        ),
    )

    override suspend fun pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Long,
    ): Boolean = set(
        collection = "programProgress",
        docId = programId,
        data = mapOf(
            "currentDay" to currentDay,
            "updatedAtEpochMillis" to updatedAtEpochMillis,
            "platform" to "android",
        ),
    )

    override suspend fun pushFlexibilityTest(
        recordedAtEpochMillis: Long,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?,
    ): Boolean = set(
        collection = "flexibilityTests",
        docId = recordedAtEpochMillis.toString(),
        data = mapOf(
            "recordedAtEpochMillis" to recordedAtEpochMillis,
            "sitAndReachCm" to sitAndReachCm,
            "toeTouchCm" to toeTouchCm,
            "shoulderReachCm" to shoulderReachCm,
            "platform" to "android",
        ),
    )
}
