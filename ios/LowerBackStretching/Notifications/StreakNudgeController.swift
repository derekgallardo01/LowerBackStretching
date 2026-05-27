import Foundation

/// Single entry point for "user toggled the streak safety net". Persists
/// the preference and (re)schedules the nudge. Mirrors the way
/// `ReminderController` wraps `ReminderManager`.
enum StreakNudgeController {
    static func apply(enabled: Bool, defaults: UserDefaults = .standard) {
        defaults.set(enabled, forKey: SettingsKeys.streakNudgeEnabled)
        if enabled {
            StreakNudgeManager.schedule()
        } else {
            StreakNudgeManager.cancel()
        }
    }
}
