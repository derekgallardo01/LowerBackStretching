import XCTest
@testable import LowerBackStretching

final class GamificationTests: XCTestCase {

    private var calendar: Calendar = {
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        cal.firstWeekday = 2 // Monday — matches Android's ISO week
        cal.minimumDaysInFirstWeek = 4
        return cal
    }()

    // MARK: XP / levels

    func testLevelOneStartsAtZeroXp() {
        XCTAssertEqual(totalXp(forLevel: 1), 0)
    }

    func testLevelTwoNeedsHundredXp() {
        XCTAssertEqual(totalXp(forLevel: 2), 100)
    }

    func testLevelFiveNeedsThousandXp() {
        XCTAssertEqual(totalXp(forLevel: 5), 1000)
    }

    func testXpForSessionRoundsDown() {
        XCTAssertEqual(xp(forSessionSeconds: 295), 29) // 29.5 -> 29
        XCTAssertEqual(xp(forSessionSeconds: 0), 0)
        XCTAssertEqual(xp(forSessionSeconds: -100), 0)
    }

    func testLevelForHandlesBoundaryValuesExactly() {
        XCTAssertEqual(level(forTotalXp: 0), 1)
        XCTAssertEqual(level(forTotalXp: 99), 1)
        XCTAssertEqual(level(forTotalXp: 100), 2)
        XCTAssertEqual(level(forTotalXp: 299), 2)
        XCTAssertEqual(level(forTotalXp: 300), 3)
        XCTAssertEqual(level(forTotalXp: 1000), 5)
    }

    func testXpProgressReportsPartialProgressCorrectly() {
        let p = xpProgress(totalXp: 150)
        XCTAssertEqual(p.level, 2)
        XCTAssertEqual(p.xpIntoLevel, 50)
        XCTAssertEqual(p.xpToNextLevel, 200)
        XCTAssertEqual(p.progress, 0.25, accuracy: 1e-3)
    }

    // MARK: Streaks

    func testLongestStreakForEmptySetIsZero() {
        XCTAssertEqual(longestStreak(days: []), 0)
    }

    func testLongestStreakForSingleDayIsOne() {
        let day = calendar.startOfDay(for: Date(timeIntervalSince1970: 86_400))
        XCTAssertEqual(longestStreak(days: [day], calendar: calendar), 1)
    }

    func testLongestStreakPicksLongerOfTwoRuns() {
        let base = Date(timeIntervalSince1970: 0)
        func at(_ offset: Int) -> Date {
            calendar.startOfDay(for: calendar.date(byAdding: .day, value: offset, to: base)!)
        }
        let days: Set<Date> = [
            at(1), at(2),                         // 2-day run
            at(10), at(11), at(12),               // 3-day run
            at(20),                                // 1-day
        ]
        XCTAssertEqual(longestStreak(days: days, calendar: calendar), 3)
    }

    func testLongestStreakCountsEveryConsecutiveDay() {
        let base = Date(timeIntervalSince1970: 0)
        let days: Set<Date> = Set((1...7).map { offset in
            calendar.startOfDay(for: calendar.date(byAdding: .day, value: offset, to: base)!)
        })
        XCTAssertEqual(longestStreak(days: days, calendar: calendar), 7)
    }

    // MARK: Weekly / monthly

    func testWeeklyCompletionsCountsOnlyDaysInTheSameWeek() {
        // Pin today to Wednesday 2025-01-15 (UTC, ISO week).
        let today = calendar.date(from: DateComponents(year: 2025, month: 1, day: 15))!
        let mon   = calendar.date(from: DateComponents(year: 2025, month: 1, day: 13))!
        let wed   = today
        let prior = calendar.date(from: DateComponents(year: 2025, month: 1, day: 6))!
        XCTAssertEqual(
            weeklyCompletions(days: [mon, wed, prior], today: today, calendar: calendar),
            2
        )
    }

    func testMonthlyCompletionsCountsOnlyTheCalendarMonth() {
        let today = calendar.date(from: DateComponents(year: 2025, month: 1, day: 15))!
        let inJan1  = calendar.date(from: DateComponents(year: 2025, month: 1, day: 1))!
        let inJan31 = calendar.date(from: DateComponents(year: 2025, month: 1, day: 31))!
        let inDec   = calendar.date(from: DateComponents(year: 2024, month: 12, day: 31))!
        let inFeb   = calendar.date(from: DateComponents(year: 2025, month: 2, day: 1))!
        XCTAssertEqual(
            monthlyCompletions(days: [inJan1, inJan31, inDec, inFeb], today: today, calendar: calendar),
            2
        )
    }
}
