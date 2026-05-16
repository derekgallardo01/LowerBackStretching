import Foundation
import SwiftUI

enum ReminderDefaults {
    static let enabledKey = "reminder_enabled"
    static let hourKey = "reminder_hour"
    static let minuteKey = "reminder_minute"

    static let hour = 8
    static let minute = 0
}

struct ReminderTime: Equatable {
    var hour: Int
    var minute: Int
}
