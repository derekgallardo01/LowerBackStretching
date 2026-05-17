import XCTest
@testable import LowerBackStretching

final class CooldownSuggestionTests: XCTestCase {

    func testOffWhenUserHasNotOptedInToReadingSteps() {
        XCTAssertFalse(shouldShowCooldown(
            enabledRead: false, stretchedToday: false, stepsToday: 9_000
        ))
    }

    func testOffWhenUserAlreadyStretchedToday() {
        XCTAssertFalse(shouldShowCooldown(
            enabledRead: true, stretchedToday: true, stepsToday: 9_000
        ))
    }

    func testOffWhenStepsAreUnknown() {
        XCTAssertFalse(shouldShowCooldown(
            enabledRead: true, stretchedToday: false, stepsToday: nil
        ))
    }

    func testOffBelowTheThreshold() {
        XCTAssertFalse(shouldShowCooldown(
            enabledRead: true, stretchedToday: false, stepsToday: 4_999
        ))
    }

    func testOnAtTheThreshold() {
        XCTAssertTrue(shouldShowCooldown(
            enabledRead: true, stretchedToday: false, stepsToday: 5_000
        ))
    }

    func testOnAboveTheThreshold() {
        XCTAssertTrue(shouldShowCooldown(
            enabledRead: true, stretchedToday: false, stepsToday: 12_345
        ))
    }

    func testThresholdIsConfigurable() {
        XCTAssertTrue(shouldShowCooldown(
            enabledRead: true, stretchedToday: false, stepsToday: 2_000,
            threshold: 1_500
        ))
    }
}
