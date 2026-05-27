import XCTest
@testable import LowerBackStretching

private struct FakeLog: PainMeasurement {
    let recordedAt: Date
    let painLevel: Int
    let bodyLocationTag: String?
    let context: String
}

/// Mirrors `android/core/src/test/java/.../PainTest.kt`. Covers the same
/// pair + delta rules so the two platforms stay in lockstep.
final class PainTests: XCTestCase {

    private let base = Date(timeIntervalSince1970: 1_700_000_000)
    private func at(_ secondsOffset: TimeInterval) -> Date { base.addingTimeInterval(secondsOffset) }

    private func pre(_ off: TimeInterval, _ level: Int = 5, tag: String? = nil) -> FakeLog {
        FakeLog(recordedAt: at(off), painLevel: level, bodyLocationTag: tag, context: PainContext.preSession)
    }

    private func post(_ off: TimeInterval, _ level: Int = 5, tag: String? = nil) -> FakeLog {
        FakeLog(recordedAt: at(off), painLevel: level, bodyLocationTag: tag, context: PainContext.postSession)
    }

    func testSessionPainDeltaComputesPostMinusPre() {
        let pair = SessionPainPair(pre: pre(0, 7), post: post(60, 4))
        XCTAssertEqual(sessionPainDelta(pair), SessionPainDelta(pre: 7, post: 4, delta: -3))
    }

    func testSessionPainDeltaReturnsNilDeltaWhenPreMissing() {
        let pair = SessionPainPair(pre: nil, post: post(60, 4))
        XCTAssertEqual(sessionPainDelta(pair), SessionPainDelta(pre: nil, post: 4, delta: nil))
    }

    func testPairPicksNearestPrecedingPreWithinWindow() {
        // Two PREs; pairing should pick the closer one (5s before POST).
        let older = pre(0, 9)
        let nearer = pre(55, 7)
        let p = post(60, 4)
        let pairs = pairSessionPainLogs([older, nearer, p])
        XCTAssertEqual(pairs.count, 1)
        XCTAssertEqual(pairs[0].pre?.painLevel, 7)
    }

    func testPairReturnsNilPreWhenNoneInsideLookback() {
        // PRE recorded 2 hours before; default lookback is 1 hour.
        let p = post(7200, 4)
        let pre1 = pre(0, 7)
        let pairs = pairSessionPainLogs([pre1, p])
        XCTAssertEqual(pairs.count, 1)
        XCTAssertNil(pairs[0].pre)
    }

    func testPairLookbackBoundaryInclusive() {
        // Exactly at the lookback boundary should still be included.
        let pre1 = pre(0, 7)
        let p = post(5000, 4)
        let pairs = pairSessionPainLogs([pre1, p], lookback: 5000)
        XCTAssertEqual(pairs[0].pre?.painLevel, 7)
    }

    func testPairIgnoresPreLogsNewerThanPost() {
        // PRE recorded after the POST should not be paired with it.
        let p = post(0, 4)
        let pre1 = pre(60, 7)
        let pairs = pairSessionPainLogs([p, pre1])
        XCTAssertNil(pairs[0].pre)
    }

    func testPairSortsResultsNewestPostFirst() {
        let p1 = post(0, 5)
        let p2 = post(1000, 4)
        let p3 = post(500, 3)
        let pairs = pairSessionPainLogs([p1, p2, p3])
        XCTAssertEqual(pairs.map { $0.post.painLevel }, [4, 3, 5])
    }
}
