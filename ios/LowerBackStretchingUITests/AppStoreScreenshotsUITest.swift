import XCTest

/// Walks the app and attaches a screenshot at each interesting screen.
/// Run via `xcodebuild test -only-testing:LowerBackStretchingUITests/AppStoreScreenshotsUITest`
/// then export the attachments from the resulting `.xcresult` bundle.
///
/// Status bar is set to the canonical demo values via
/// `xcrun simctl status_bar override` from the caller (see
/// `scripts/capture-screenshots.sh`).
final class AppStoreScreenshotsUITest: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testCaptureAppScreenshots() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-onboarding_done", "YES", "-resetData"]
        app.launch()

        // Tab 1 — Home (welcome, quick cards, programs)
        XCTAssertTrue(tabButton(app, "Home").waitForExistence(timeout: 5))
        capture(name: "01-home")

        // Tab 2 — Programs (built-in catalog)
        tabButton(app, "Programs").tap()
        _ = app.staticTexts["Lower Back Relief"].waitForExistence(timeout: 3)
        capture(name: "02-programs")

        // Program detail
        app.staticTexts["Lower Back Relief"].tap()
        _ = app.navigationBars.element(boundBy: 0).waitForExistence(timeout: 3)
        sleep(1)
        capture(name: "03-program-detail")
        app.navigationBars.buttons.element(boundBy: 0).tap()

        // Tab 3 — Stretches (filterable catalog)
        tabButton(app, "Stretches").tap()
        _ = app.staticTexts["Cat-Cow"].waitForExistence(timeout: 3)
        capture(name: "04-stretches")

        // Stretch detail (Cat-Cow — shows body diagram + content)
        app.staticTexts["Cat-Cow"].tap()
        sleep(1)
        capture(name: "05-stretch-detail")
        app.navigationBars.buttons.element(boundBy: 0).tap()

        // Tab 4 — Calendar (empty state but layout visible)
        tabButton(app, "Calendar").tap()
        sleep(1)
        capture(name: "06-calendar")

        // Tab 5 — Settings (sections collapsed/expanded)
        tabButton(app, "Settings").tap()
        sleep(1)
        capture(name: "07-settings")

        // Back to Home → scroll to the second row of Quick Cards → tap Pain log
        tabButton(app, "Home").tap()
        sleep(1)
        let painLog = app.staticTexts["Pain log"]
        if painLog.exists {
            painLog.tap()
            sleep(1)
            capture(name: "08-pain-log")
        }
    }

    /// On iPhone, tabs live in `app.tabBars`. On iPad (iOS 18+), the same
    /// `TabView` items render as both a sidebar entry AND a bottom-bar
    /// entry simultaneously, so a name lookup matches twice. `firstMatch`
    /// works on both.
    private func tabButton(_ app: XCUIApplication, _ name: String) -> XCUIElement {
        let inBar = app.tabBars.buttons[name].firstMatch
        if inBar.waitForExistence(timeout: 1) { return inBar }
        return app.buttons[name].firstMatch
    }

    func testCaptureOnboardingScreenshots() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-onboarding_done", "NO", "-resetData"]
        app.launch()
        sleep(1)

        // Page 1 — Safety check (the Android-parity addition)
        capture(name: "09-onboarding-safety")
    }

    private func capture(name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
