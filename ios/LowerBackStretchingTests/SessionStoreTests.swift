import XCTest
@testable import LowerBackStretching

final class SessionStoreTests: XCTestCase {

    private var cal: Calendar!
    private var today: Date!

    override func setUp() {
        super.setUp()
        cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        today = cal.startOfDay(for: cal.date(from: DateComponents(year: 2026, month: 5, day: 15))!)
    }

    private func daysBack(_ n: Int) -> Date {
        cal.date(byAdding: .day, value: -n, to: today)!
    }

    func testEmptyReturnsZero() {
        XCTAssertEqual(SessionStore.streak(from: [], today: today, calendar: cal), 0)
    }

    func testOnlyTodayReturnsOne() {
        XCTAssertEqual(SessionStore.streak(from: [today], today: today, calendar: cal), 1)
    }

    func testThreeConsecutiveDaysReturnsThree() {
        let days: Set<Date> = [daysBack(0), daysBack(1), daysBack(2)]
        XCTAssertEqual(SessionStore.streak(from: days, today: today, calendar: cal), 3)
    }

    func testTodayMissingButYesterdayPresentUsesGraceDay() {
        let days: Set<Date> = [daysBack(1), daysBack(2)]
        XCTAssertEqual(SessionStore.streak(from: days, today: today, calendar: cal), 2)
    }

    func testGapBreaksStreak() {
        let days: Set<Date> = [daysBack(0), daysBack(2)]
        XCTAssertEqual(SessionStore.streak(from: days, today: today, calendar: cal), 1)
    }

    func testOldDaysOnlyReturnsZero() {
        let days: Set<Date> = [daysBack(5), daysBack(6)]
        XCTAssertEqual(SessionStore.streak(from: days, today: today, calendar: cal), 0)
    }

    func testTenConsecutiveDays() {
        let days = Set((0..<10).map { daysBack($0) })
        XCTAssertEqual(SessionStore.streak(from: days, today: today, calendar: cal), 10)
    }
}
