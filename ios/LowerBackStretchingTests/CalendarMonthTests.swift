import XCTest
@testable import LowerBackStretching

final class CalendarMonthTests: XCTestCase {

    private var cal: Calendar!

    override func setUp() {
        super.setUp()
        // Use a deterministic timezone and Monday-first week.
        cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        cal.firstWeekday = 2 // Monday
    }

    private func date(_ year: Int, _ month: Int, _ day: Int = 1) -> Date {
        cal.date(from: DateComponents(year: year, month: month, day: day))!
    }

    func testWeeksCoverFullMonthAnd35Or42Cells() {
        let may2026 = CalendarMonth(month: date(2026, 5), calendar: cal)
        let cells = may2026.weeks.flatMap { $0 }
        XCTAssertTrue(cells.count == 35 || cells.count == 42)
        // 31 days in May 2026 should all be present.
        XCTAssertEqual(cells.filter { may2026.isInMonth($0) }.count, 31)
    }

    func testWeekdaySymbolsHasSevenEntries() {
        // English narrow weekday symbols collide (M T W T F S S) — so the
        // labels are NOT all unique. Just verify the count.
        let m = CalendarMonth(month: date(2026, 5), calendar: cal)
        XCTAssertEqual(m.weekdaySymbols.count, 7)
    }

    func testIsInMonthForBoundaryDays() {
        let m = CalendarMonth(month: date(2026, 5, 15), calendar: cal)
        XCTAssertTrue(m.isInMonth(date(2026, 5, 1)))
        XCTAssertTrue(m.isInMonth(date(2026, 5, 31)))
        XCTAssertFalse(m.isInMonth(date(2026, 4, 30)))
        XCTAssertFalse(m.isInMonth(date(2026, 6, 1)))
    }

    func testFebruaryLeapYear() {
        let feb2024 = CalendarMonth(month: date(2024, 2), calendar: cal)
        let inMonth = feb2024.weeks.flatMap { $0 }.filter { feb2024.isInMonth($0) }
        XCTAssertEqual(inMonth.count, 29)
    }

    func testFebruaryNonLeapYear() {
        let feb2025 = CalendarMonth(month: date(2025, 2), calendar: cal)
        let inMonth = feb2025.weeks.flatMap { $0 }.filter { feb2025.isInMonth($0) }
        XCTAssertEqual(inMonth.count, 28)
    }
}
