import XCTest
@testable import LowerBackStretching

/// `ReminderController.apply` should always persist preferences (regardless
/// of the schedule-vs-cancel branch). The OS notification side is hard to
/// assert in a unit test; `nextOccurrence` math has its own coverage on the
/// Android side and the iOS API is a thin wrapper around UNCalendarNotificationTrigger.
final class ReminderControllerTests: XCTestCase {

    private var suite: String!
    private var defaults: UserDefaults!

    override func setUp() {
        super.setUp()
        // Use an isolated suite so we don't fight with real user prefs.
        suite = "ReminderControllerTests.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suite)
        UserDefaults.standard.removePersistentDomain(forName: suite)
    }

    override func tearDown() {
        UserDefaults.standard.removePersistentDomain(forName: suite)
        super.tearDown()
    }

    func testApplyEnabledPersistsAllFields() {
        ReminderController.apply(enabled: true, hour: 9, minute: 15)
        XCTAssertTrue(UserDefaults.standard.bool(forKey: ReminderDefaults.enabledKey))
        XCTAssertEqual(UserDefaults.standard.integer(forKey: ReminderDefaults.hourKey), 9)
        XCTAssertEqual(UserDefaults.standard.integer(forKey: ReminderDefaults.minuteKey), 15)
    }

    func testApplyDisabledPersistsTimeButDisables() {
        ReminderController.apply(enabled: true, hour: 9, minute: 15)
        ReminderController.apply(enabled: false, hour: 9, minute: 15)
        XCTAssertFalse(UserDefaults.standard.bool(forKey: ReminderDefaults.enabledKey))
        XCTAssertEqual(UserDefaults.standard.integer(forKey: ReminderDefaults.hourKey), 9)
    }
}
