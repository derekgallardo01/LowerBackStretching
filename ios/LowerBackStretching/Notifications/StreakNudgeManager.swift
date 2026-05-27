import Foundation
import UserNotifications

/// Daily evening "you'll lose your streak" nudge. Counterpart to the
/// Android `StreakNudgeReceiver`.
///
/// iOS doesn't expose a pre-fire hook for background notifications, so
/// the gate (streak >= 3, no session today, opted in) is enforced at
/// schedule time and re-evaluated lazily in `NotificationDelegate`'s
/// foreground hook. The repeating 20:00 trigger keeps things simple;
/// `clearDelivered()` after a session removes the day's nudge from the
/// tray if it already fired.
enum StreakNudgeManager {
    static let identifier = "streak_nudge_reminder"
    static let fireHour = 20

    static func schedule() {
        cancel()
        let center = UNUserNotificationCenter.current()
        center.requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in
            let content = UNMutableNotificationContent()
            content.title = "Your stretch streak is at risk"
            content.body = "Three minutes is enough to keep it alive."
            content.sound = .default

            var components = DateComponents()
            components.hour = fireHour
            components.minute = 0

            let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
            let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
            center.add(request)
        }
    }

    static func cancel() {
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [identifier])
    }

    /// Drops any delivered nudge from the notification tray. Called by
    /// SessionStore after the user records a session so they aren't
    /// reminded of something they already did.
    static func clearDelivered() {
        UNUserNotificationCenter.current()
            .removeDeliveredNotifications(withIdentifiers: [identifier])
    }
}

/// Pure-function gate for the streak nudge. Same shape as the Android
/// `shouldNudgeStreak()` so the tests line up across platforms. The
/// foreground delegate calls this just before deciding whether to
/// surface a banner.
func shouldNudgeStreak(
    enabled: Bool,
    lastSessionEpochDay: Int,
    todayEpochDay: Int,
    currentStreak: Int,
    minimumStreak: Int = 3
) -> Bool {
    guard enabled else { return false }
    guard lastSessionEpochDay != todayEpochDay else { return false }
    return currentStreak >= minimumStreak
}
