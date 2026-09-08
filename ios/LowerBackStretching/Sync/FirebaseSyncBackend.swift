import Foundation
import FirebaseAuth
import FirebaseFirestore

/// Real `SyncBackend` against Firebase: anonymous auth + Firestore writes to
/// the per-user tree the rules in `firebase/firestore.rules` were written for
/// (`/users/{uid}/sessions|routines|programProgress|flexibilityTests`).
///
/// Document ids are deterministic (timestamps / local ids), so retried pushes
/// are idempotent upserts rather than duplicates. Every method is best-effort:
/// failures return false/nil and never throw — SwiftData stays canonical, the
/// cloud copy is a mirror. Only assigned to `SyncController.shared.backend`
/// when `GoogleService-Info.plist` is bundled (see `LowerBackStretchingApp`).
struct FirebaseSyncBackend: SyncBackend {

    func signedInUid() async -> String? {
        Auth.auth().currentUser?.uid
    }

    func signInAnonymously() async -> String? {
        do {
            return try await Auth.auth().signInAnonymously().user.uid
        } catch {
            return nil
        }
    }

    func signOut() async {
        try? Auth.auth().signOut()
    }

    private func set(collection: String, docId: String, data: [String: Any]) async -> Bool {
        var uid = await signedInUid()
        if uid == nil { uid = await signInAnonymously() }
        guard let uid else { return false }
        do {
            try await Firestore.firestore()
                .collection("users").document(uid)
                .collection(collection).document(docId)
                .setData(data, merge: true)
            return true
        } catch {
            return false
        }
    }

    func pushSession(
        programId: String,
        dayNumber: Int,
        durationSeconds: Int,
        completedAtEpochMillis: Int64,
        type: String
    ) async -> Bool {
        await set(
            collection: "sessions",
            docId: String(completedAtEpochMillis),
            data: [
                "programId": programId,
                "dayNumber": dayNumber,
                "durationSeconds": durationSeconds,
                "completedAtEpochMillis": completedAtEpochMillis,
                "type": type,
                "platform": "ios",
            ]
        )
    }

    func pushRoutine(
        localId: String,
        name: String,
        stretchIds: [String],
        displayOrder: Int,
        deletedAtEpochMillis: Int64?
    ) async -> Bool {
        var data: [String: Any] = [
            "name": name,
            "stretchIds": stretchIds,
            "displayOrder": displayOrder,
            "platform": "ios",
        ]
        if let deletedAtEpochMillis { data["deletedAtEpochMillis"] = deletedAtEpochMillis }
        return await set(collection: "routines", docId: localId, data: data)
    }

    func pushProgramProgress(
        programId: String,
        currentDay: Int,
        updatedAtEpochMillis: Int64
    ) async -> Bool {
        await set(
            collection: "programProgress",
            docId: programId,
            data: [
                "currentDay": currentDay,
                "updatedAtEpochMillis": updatedAtEpochMillis,
                "platform": "ios",
            ]
        )
    }

    func pushFlexibilityTest(
        recordedAtEpochMillis: Int64,
        sitAndReachCm: Float?,
        toeTouchCm: Float?,
        shoulderReachCm: Float?
    ) async -> Bool {
        var data: [String: Any] = [
            "recordedAtEpochMillis": recordedAtEpochMillis,
            "platform": "ios",
        ]
        if let sitAndReachCm { data["sitAndReachCm"] = sitAndReachCm }
        if let toeTouchCm { data["toeTouchCm"] = toeTouchCm }
        if let shoulderReachCm { data["shoulderReachCm"] = shoulderReachCm }
        return await set(collection: "flexibilityTests", docId: String(recordedAtEpochMillis), data: data)
    }
}
