import Foundation

/// Single source of truth for "user changed the reminder". Persists the
/// preference to UserDefaults (the same store @AppStorage reads/writes) and
/// re-(scheduling|cancelling) the UNCalendarNotificationTrigger so the two
/// never drift. Called by Settings and Onboarding.
enum ReminderController {
    static func apply(enabled: Bool, hour: Int, minute: Int) {
        let defaults = UserDefaults.standard
        defaults.set(enabled, forKey: ReminderDefaults.enabledKey)
        defaults.set(hour,    forKey: ReminderDefaults.hourKey)
        defaults.set(minute,  forKey: ReminderDefaults.minuteKey)

        if enabled {
            ReminderManager.schedule(hour: hour, minute: minute)
        } else {
            ReminderManager.cancel()
        }
    }
}
