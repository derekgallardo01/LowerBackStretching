import XCTest
@testable import LowerBackStretching

/// `ReminderController.apply` should always persist preferences (regardless
/// of the schedule-vs-cancel branch). The OS notification side is hard to
/// assert in a unit test; `nextOccurrence` math has its own coverage on the
/// Android side and the iOS API is a thin wrapper around UNCalendarNotificationTrigger.
final class ReminderControllerTests: XCTestCase {

    private var suiteName: String!
    private var defaults: UserDefaults!

    override func setUp() {
        super.setUp()
        suiteName = "ReminderControllerTests.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        super.tearDown()
    }

    func testApplyEnabledPersistsAllFields() {
        ReminderController.apply(enabled: true, hour: 9, minute: 15, defaults: defaults)
        XCTAssertTrue(defaults.bool(forKey: ReminderDefaults.enabledKey))
        XCTAssertEqual(defaults.integer(forKey: ReminderDefaults.hourKey), 9)
        XCTAssertEqual(defaults.integer(forKey: ReminderDefaults.minuteKey), 15)
    }

    func testApplyDisabledPersistsTimeButDisables() {
        ReminderController.apply(enabled: true, hour: 9, minute: 15, defaults: defaults)
        ReminderController.apply(enabled: false, hour: 9, minute: 15, defaults: defaults)
        XCTAssertFalse(defaults.bool(forKey: ReminderDefaults.enabledKey))
        XCTAssertEqual(defaults.integer(forKey: ReminderDefaults.hourKey), 9)
    }
}
