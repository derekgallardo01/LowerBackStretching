import XCTest
@testable import LowerBackStretching

final class EpochDayTests: XCTestCase {

    private var utc: Calendar = {
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        return cal
    }()

    func testEpochDayForUnixZeroIsZero() {
        let date = Date(timeIntervalSince1970: 0)
        XCTAssertEqual(EpochDay.current(date, calendar: utc), 0)
    }

    func testEpochDayForOneDayAfterEpochIsOne() {
        let date = Date(timeIntervalSince1970: 86_400)
        XCTAssertEqual(EpochDay.current(date, calendar: utc), 1)
    }

    func testEpochDayIsStableThroughoutASingleDay() {
        let morning = Date(timeIntervalSince1970: 86_400 + 60)        // day 1, 00:01 UTC
        let evening = Date(timeIntervalSince1970: 86_400 + 86_399)    // day 1, 23:59 UTC
        XCTAssertEqual(EpochDay.current(morning, calendar: utc), EpochDay.current(evening, calendar: utc))
    }

    func testEpochDayAdvancesAtMidnight() {
        let dayOne = Date(timeIntervalSince1970: 86_400 + 86_399)     // day 1, 23:59:59
        let dayTwo = Date(timeIntervalSince1970: 2 * 86_400)          // day 2, 00:00:00
        XCTAssertEqual(EpochDay.current(dayTwo, calendar: utc) - EpochDay.current(dayOne, calendar: utc), 1)
    }
}
