import XCTest

final class OnboardingE2EUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testSkipOnboardingLandsOnHome() throws {
        let app = XCUIApplication()
        // Reset onboarding state so the flow always shows for the test.
        app.launchArguments += ["-onboarding_done", "NO"]
        app.launch()

        let skip = app.buttons["Skip"]
        XCTAssertTrue(skip.waitForExistence(timeout: 5), "Skip button should be visible on first launch")
        skip.tap()

        // Tab bar items should appear.
        XCTAssertTrue(app.tabBars.buttons["Home"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.tabBars.buttons["Programs"].exists)
        XCTAssertTrue(app.tabBars.buttons["Calendar"].exists)
    }

    func testStepThroughAllPagesCompletesOnboarding() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-onboarding_done", "NO"]
        app.launch()

        // 4 pages — tap Next 3 times, then "Turn on reminders".
        let next = app.buttons["Next"]
        XCTAssertTrue(next.waitForExistence(timeout: 5))
        next.tap()
        next.tap()
        next.tap()

        app.buttons["Turn on reminders"].tap()

        XCTAssertTrue(app.tabBars.buttons["Programs"].waitForExistence(timeout: 3))
    }

    func testCreatingACustomRoutine() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-onboarding_done", "YES"]
        app.launch()

        app.tabBars.buttons["Programs"].tap()

        // Open the routine builder via the toolbar "+".
        // The button title is "New routine" from the Label, but the icon-only
        // toolbar renders just the symbol — query by descendant.
        let newButton = app.buttons["New routine"]
        XCTAssertTrue(newButton.waitForExistence(timeout: 3))
        newButton.tap()

        let nameField = app.textFields["Routine name"]
        XCTAssertTrue(nameField.waitForExistence(timeout: 3))
        nameField.tap()
        nameField.typeText("Morning")

        // Save button is disabled until at least one stretch is selected.
        let firstStretch = app.cells.element(boundBy: 0)
        XCTAssertTrue(firstStretch.waitForExistence(timeout: 3))
        firstStretch.tap()

        app.buttons["Save"].tap()

        // After dismiss, the new routine should be in the "My routines" section.
        XCTAssertTrue(app.staticTexts["Morning"].waitForExistence(timeout: 3))
    }
}
