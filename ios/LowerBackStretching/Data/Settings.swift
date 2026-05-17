import Foundation
import SwiftUI

/// All `@AppStorage` keys plus their defaults live here so the rest of
/// the app doesn't repeat string literals. The existing
/// `ReminderDefaults` keys are kept for backward-compat; new code uses
/// `SettingsKeys`.
enum SettingsKeys {
    static let reminderEnabled = ReminderDefaults.enabledKey
    static let reminderHour    = ReminderDefaults.hourKey
    static let reminderMinute  = ReminderDefaults.minuteKey

    static let onboardingDone = "onboarding_done"

    static let themeMode = "theme_mode"
    static let durationUnit = "duration_unit"

    static let hapticsTransitions = "haptics_transitions"
    static let hapticsFinish = "haptics_finish"

    static let musicTrack = "music_track"
    static let musicVolume = "music_volume"
    static let ambientTrack = "ambient_track"
    static let ambientVolume = "ambient_volume"
    static let chimeTrack = "chime_track"

    /// Days since 1970-01-01 (local calendar) of the user's most
    /// recent recorded session. Smart-reminder gate reads this on
    /// notification delivery.
    static let lastSessionEpochDay = "last_session_epoch_day"

    static let weeklyGoal = "weekly_goal"
    static let monthlyGoal = "monthly_goal"

    static let healthWriteEnabled = "health_write_enabled"
    static let healthReadEnabled = "health_read_enabled"
}

enum GoalDefaults {
    static let weekly = 3
    static let monthly = 12
}

/// Days since 1970-01-01 in the user's current calendar. Used for
/// "did the user stretch today" checks on both write (SessionStore)
/// and read (notification delegate).
enum EpochDay {
    static func current(_ date: Date = .now, calendar: Calendar = .current) -> Int {
        let start = calendar.startOfDay(for: date)
        let epoch = Date(timeIntervalSince1970: 0)
        return calendar.dateComponents([.day], from: epoch, to: start).day ?? 0
    }
}

enum ThemeMode: String, CaseIterable {
    case system, light, dark

    var storageValue: String { rawValue }

    static func fromStorage(_ value: String?) -> ThemeMode {
        ThemeMode(rawValue: value ?? "") ?? .system
    }

    /// SwiftUI color scheme override. `nil` means "follow the system".
    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }
}

enum DurationUnit: String, CaseIterable {
    case seconds
    case minutesShort = "minutes_short"

    var storageValue: String { rawValue }

    static func fromStorage(_ value: String?) -> DurationUnit {
        DurationUnit(rawValue: value ?? "") ?? .seconds
    }
}
