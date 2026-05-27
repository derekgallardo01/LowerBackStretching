import XCTest
@testable import LowerBackStretching

/// Mirrors `ShouldNudgeStreakTest.kt`. Same conditions, same expected
/// truth table — the predicate exists so both platforms can be checked
/// against the same fixtures.
final class StreakNudgeTests: XCTestCase {

    private let today = 20_000
    private let yesterday = 19_999

    func testReturnsFalseWhenUserOptedOut() {
        XCTAssertFalse(shouldNudgeStreak(
            enabled: false,
            lastSessionEpochDay: yesterday,
            todayEpochDay: today,
            currentStreak: 5
        ))
    }

    func testReturnsFalseWhenUserAlreadyStretchedToday() {
        XCTAssertFalse(shouldNudgeStreak(
            enabled: true,
            lastSessionEpochDay: today,
            todayEpochDay: today,
            currentStreak: 5
        ))
    }

    func testReturnsFalseWhenStreakIsBelowThreshold() {
        XCTAssertFalse(shouldNudgeStreak(
            enabled: true,
            lastSessionEpochDay: yesterday,
            todayEpochDay: today,
            currentStreak: 2
        ))
    }

    func testReturnsTrueWhenStreakAtRiskAndOptedIn() {
        XCTAssertTrue(shouldNudgeStreak(
            enabled: true,
            lastSessionEpochDay: yesterday,
            todayEpochDay: today,
            currentStreak: 3
        ))
    }

    func testReturnsTrueForLongStreak() {
        XCTAssertTrue(shouldNudgeStreak(
            enabled: true,
            lastSessionEpochDay: yesterday,
            todayEpochDay: today,
            currentStreak: 365
        ))
    }

    func testNeverStretchedWithZeroDefaultReturnsFalse() {
        XCTAssertFalse(shouldNudgeStreak(
            enabled: true,
            lastSessionEpochDay: 0,
            todayEpochDay: today,
            currentStreak: 0
        ))
    }
}
