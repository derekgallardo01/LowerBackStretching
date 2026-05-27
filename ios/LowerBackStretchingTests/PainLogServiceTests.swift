import XCTest
@testable import LowerBackStretching

/// Mirrors the `hasPreLoggedToday(...)` portion of
/// `PainLogRepositoryTest.kt`. Insertion side is covered implicitly by
/// the SwiftData ModelContext — the gating logic is the bit worth
/// pinning down in isolation.
final class PainLogServiceTests: XCTestCase {

    private var cal: Calendar!
    private var noon: Date!

    override func setUp() {
        super.setUp()
        cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        noon = cal.date(from: DateComponents(year: 2026, month: 5, day: 15, hour: 12))!
    }

    func testReturnsFalseOnEmptyStore() {
        XCTAssertFalse(PainLogService.hasPreLoggedToday(logs: [], now: noon, calendar: cal))
    }

    func testReturnsTrueAfterPreLogToday() {
        let pre = PainLog(
            recordedAt: noon.addingTimeInterval(-3600),
            painLevel: 5,
            bodyLocationTag: nil,
            context: PainContext.preSession
        )
        XCTAssertTrue(PainLogService.hasPreLoggedToday(logs: [pre], now: noon, calendar: cal))
    }

    func testIgnoresPostLogs() {
        // Even with a POST already recorded today, we still need a PRE.
        let post = PainLog(
            recordedAt: noon.addingTimeInterval(-1800),
            painLevel: 3,
            bodyLocationTag: nil,
            context: PainContext.postSession
        )
        XCTAssertFalse(PainLogService.hasPreLoggedToday(logs: [post], now: noon, calendar: cal))
    }

    func testRespectsStartOfDayBoundary() {
        let yesterdayLate = cal.date(from: DateComponents(year: 2026, month: 5, day: 14, hour: 23))!
        let pre = PainLog(
            recordedAt: yesterdayLate,
            painLevel: 5,
            bodyLocationTag: nil,
            context: PainContext.preSession
        )
        XCTAssertFalse(PainLogService.hasPreLoggedToday(logs: [pre], now: noon, calendar: cal))
    }
}
