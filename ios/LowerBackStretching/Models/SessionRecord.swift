import Foundation
import SwiftData

@Model
final class SessionRecord {
    var programId: String
    var dayNumber: Int
    var completedAt: Date
    var durationSeconds: Int
    /// `SessionType.storageValue` — "program" / "single" / "routine".
    /// Defaulted so SwiftData lightweight migration covers existing rows.
    var type: String = SessionType.program.storageValue

    init(
        programId: String,
        dayNumber: Int,
        completedAt: Date = .now,
        durationSeconds: Int,
        type: SessionType = .program
    ) {
        self.programId = programId
        self.dayNumber = dayNumber
        self.completedAt = completedAt
        self.durationSeconds = durationSeconds
        self.type = type.storageValue
    }

    var completedDay: Date {
        Calendar.current.startOfDay(for: completedAt)
    }
}
