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
