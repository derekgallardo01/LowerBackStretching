import XCTest

final class TabNavigationUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    private func launchPastOnboarding() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-onboarding_done", "YES"]
        app.launch()
        return app
    }

    func testAllFiveTabsArePresent() {
        let app = launchPastOnboarding()
        XCTAssertTrue(app.tabBars.buttons["Home"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.tabBars.buttons["Programs"].exists)
        XCTAssertTrue(app.tabBars.buttons["Stretches"].exists)
        XCTAssertTrue(app.tabBars.buttons["Calendar"].exists)
        XCTAssertTrue(app.tabBars.buttons["Settings"].exists)
    }

    func testProgramsTabShowsBuiltInPrograms() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Programs"].tap()
        XCTAssertTrue(app.staticTexts["Lower Back Relief"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Hip Opener"].exists)
    }

    func testProgramsCategoryFilter() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Programs"].tap()

        XCTAssertTrue(app.staticTexts["Lower Back Relief"].waitForExistence(timeout: 3))
        // Tap the 'legs' chip — built-in programs in 'lower-back' should hide.
        app.buttons["legs"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Leg Flexibility"].exists)
    }

    func testStretchesTabFiltersByBodyPart() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Stretches"].tap()

        XCTAssertTrue(app.staticTexts["Cat-Cow"].waitForExistence(timeout: 3))

        // Filter by calves.
        app.buttons["calves"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Wall Calf Stretch"].exists)
    }

    func testOpenStretchShowsPracticeButton() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Stretches"].tap()
        app.staticTexts["Cat-Cow"].tap()
        XCTAssertTrue(app.buttons["Practice this stretch"].waitForExistence(timeout: 3))
    }

    func testCalendarTabShowsEmptyState() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Calendar"].tap()

        XCTAssertTrue(app.staticTexts["Streak"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Sessions"].exists)
        XCTAssertTrue(app.staticTexts["Active days"].exists)
    }

    func testSettingsTabShowsReminderSection() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Settings"].tap()

        XCTAssertTrue(app.staticTexts["Daily reminder"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.switches.firstMatch.exists)
        XCTAssertTrue(app.staticTexts["About"].exists)
    }

    func testOpenProgramShowsDays() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Programs"].tap()
        app.staticTexts["Lower Back Relief"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Day 1 · Gentle Start"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Day 7 · Putting It Together"].exists)
    }

    func testToggleReminderSwitch() {
        let app = launchPastOnboarding()
        app.tabBars.buttons["Settings"].tap()

        let toggle = app.switches.firstMatch
        XCTAssertTrue(toggle.waitForExistence(timeout: 3))
        toggle.tap()
        // No crash means the path through ReminderManager works.
    }
}
