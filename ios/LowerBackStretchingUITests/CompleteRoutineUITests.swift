import XCTest

/// Full happy-path: skip onboarding → Programs → Daily 5-Minute → Day 1
/// → skip through all stretches → Done → Calendar → recent session
/// shows the program.
final class CompleteRoutineUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testCompletingARoutineShowsInCalendar() throws {
        let app = XCUIApplication()
        app.launchArguments += [
            "-onboarding_done", "YES",
            "-resetData",          // wipes SwiftData so the counts are deterministic
        ]
        app.launch()

        app.tabBars.buttons["Programs"].tap()
        app.staticTexts["Daily 5-Minute"].firstMatch.tap()
        app.staticTexts["Day 1 · Daily Routine"].firstMatch.tap()

        let next = app.buttons["playerNext"]
        XCTAssertTrue(next.waitForExistence(timeout: 5))
        for _ in 0..<4 { next.tap() }

        // Finish screen.
        XCTAssertTrue(app.staticTexts["Nice work."].waitForExistence(timeout: 3))
        app.buttons["Done"].tap()

        // Calendar should now have the session.
        app.tabBars.buttons["Calendar"].tap()
        XCTAssertTrue(app.staticTexts["Recent sessions"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Daily 5-Minute · Day 1"].exists)
    }
}
