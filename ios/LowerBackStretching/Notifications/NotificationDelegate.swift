import Foundation
import UserNotifications

/// Foreground notification gate. When the app is in the foreground and
/// the user has already stretched today, suppress the daily reminder
/// banner. Background delivery still occurs — iOS doesn't give us a
/// pre-delivery hook outside the foreground — but the user will still
/// see "you already did it" reflected in the app when they open it.
///
/// Installed as `UNUserNotificationCenter.current().delegate` from the
/// app's root scene.
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    static let shared = NotificationDelegate()
    private override init() {}

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let id = notification.request.identifier
        if id == ReminderManager.identifier && alreadyStretchedToday() {
            completionHandler([])
            return
        }
        if id == StreakNudgeManager.identifier && !shouldShowStreakNudge() {
            completionHandler([])
            return
        }
        completionHandler([.banner, .sound, .badge])
    }

    private func alreadyStretchedToday() -> Bool {
        let last = UserDefaults.standard.integer(forKey: SettingsKeys.lastSessionEpochDay)
        return last == EpochDay.current()
    }

    /// Foreground gate for the evening streak nudge — runs the same
    /// `shouldNudgeStreak(...)` predicate used in tests, reading current
    /// streak from `StreakNudgeForegroundGate.currentStreak()`.
    private func shouldShowStreakNudge() -> Bool {
        let defaults = UserDefaults.standard
        let enabled = defaults.object(forKey: SettingsKeys.streakNudgeEnabled) as? Bool ?? true
        let last = defaults.integer(forKey: SettingsKeys.lastSessionEpochDay)
        let streak = StreakNudgeForegroundGate.currentStreak()
        return shouldNudgeStreak(
            enabled: enabled,
            lastSessionEpochDay: last,
            todayEpochDay: EpochDay.current(),
            currentStreak: streak
        )
    }
}

/// Bridges the foreground delegate (no access to a SwiftData ModelContext)
/// to the current streak by reading the snapshot a SwiftUI view stashed
/// the last time it queried sessions. Views that observe `@Query
/// SessionRecord` should call `setStreak(_:)` whenever they re-render so
/// the foreground gate has fresh data.
enum StreakNudgeForegroundGate {
    private static let key = "streak_nudge_last_known_streak"

    static func setStreak(_ streak: Int) {
        UserDefaults.standard.set(streak, forKey: key)
    }

    static func currentStreak() -> Int {
        UserDefaults.standard.integer(forKey: key)
    }
}
