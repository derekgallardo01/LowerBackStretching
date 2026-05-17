import XCTest
@testable import LowerBackStretching

final class AchievementsTests: XCTestCase {

    func testNothingUnlockedAtZeroStats() {
        let statuses = evaluateAchievements(totalSessions: 0, longestStreak: 0, level: 1)
        XCTAssertTrue(statuses.allSatisfy { !$0.unlocked })
    }

    func testFirstSessionUnlocksOnTheFirstCompletion() {
        let statuses = evaluateAchievements(totalSessions: 1, longestStreak: 1, level: 1)
        let first = statuses.first { $0.achievement.id == .firstSession }!
        XCTAssertTrue(first.unlocked)
        XCTAssertEqual(first.progress, 1)
    }

    func testSevenDayStreakUnlocksAtSeven() {
        let statuses = evaluateAchievements(totalSessions: 10, longestStreak: 7, level: 1)
        let a = statuses.first { $0.achievement.id == .sevenDayStreak }!
        XCTAssertTrue(a.unlocked)
    }

    func testLockedAchievementStillReportsProgressUpToTarget() {
        let statuses = evaluateAchievements(totalSessions: 23, longestStreak: 3, level: 2)
        let fifty = statuses.first { $0.achievement.id == .fiftySessions }!
        XCTAssertFalse(fifty.unlocked)
        XCTAssertEqual(fifty.progress, 23)

        let seven = statuses.first { $0.achievement.id == .sevenDayStreak }!
        XCTAssertFalse(seven.unlocked)
        XCTAssertEqual(seven.progress, 3)
    }

    func testProgressIsCappedAtTargetSoUiBarDoesNotOverflow() {
        let statuses = evaluateAchievements(totalSessions: 999, longestStreak: 999, level: 99)
        for status in statuses {
            XCTAssertLessThanOrEqual(status.progress, status.achievement.target)
            XCTAssertTrue(status.unlocked)
        }
    }

    func testLevelAchievementsKeyOffLevelNotXp() {
        let statuses = evaluateAchievements(totalSessions: 1, longestStreak: 0, level: 5)
        let levelFive = statuses.first { $0.achievement.id == .levelFive }!
        let levelTen = statuses.first { $0.achievement.id == .levelTen }!
        XCTAssertTrue(levelFive.unlocked)
        XCTAssertFalse(levelTen.unlocked)
        XCTAssertEqual(levelTen.progress, 5)
    }
}
