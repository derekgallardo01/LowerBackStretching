import XCTest
@testable import LowerBackStretching

final class SyncBackendTests: XCTestCase {

    func testNoopSignedInUidReturnsNil() async {
        let uid = await NoopSyncBackend().signedInUid()
        XCTAssertNil(uid)
    }

    func testNoopSignInAnonymouslyReturnsNil() async {
        let uid = await NoopSyncBackend().signInAnonymously()
        XCTAssertNil(uid)
    }

    func testNoopSignOutCompletesWithoutError() async {
        await NoopSyncBackend().signOut() // should not throw
    }

    func testNoopPushSessionReturnsFalse() async {
        let ok = await NoopSyncBackend().pushSession(
            programId: "lower-back-relief-7day",
            dayNumber: 1,
            durationSeconds: 300,
            completedAtEpochMillis: 1_700_000_000_000,
            type: "program"
        )
        XCTAssertFalse(ok)
    }

    func testNoopPushRoutineReturnsFalse() async {
        let ok = await NoopSyncBackend().pushRoutine(
            localId: "uuid",
            name: "Morning",
            stretchIds: ["cat-cow"],
            displayOrder: 0,
            deletedAtEpochMillis: nil
        )
        XCTAssertFalse(ok)
    }

    func testNoopPushProgramProgressReturnsFalse() async {
        let ok = await NoopSyncBackend().pushProgramProgress(
            programId: "p1",
            currentDay: 2,
            updatedAtEpochMillis: 0
        )
        XCTAssertFalse(ok)
    }

    func testNoopPushFlexibilityTestReturnsFalse() async {
        let ok = await NoopSyncBackend().pushFlexibilityTest(
            recordedAtEpochMillis: 0,
            sitAndReachCm: 10,
            toeTouchCm: nil,
            shoulderReachCm: nil
        )
        XCTAssertFalse(ok)
    }
}
