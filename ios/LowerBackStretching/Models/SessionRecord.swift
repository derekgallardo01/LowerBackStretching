import Foundation
import SwiftData

@Model
final class SessionRecord {
    var programId: String
    var dayNumber: Int
    var completedAt: Date
    var durationSeconds: Int

    init(programId: String, dayNumber: Int, completedAt: Date = .now, durationSeconds: Int) {
        self.programId = programId
        self.dayNumber = dayNumber
        self.completedAt = completedAt
        self.durationSeconds = durationSeconds
    }

    var completedDay: Date {
        Calendar.current.startOfDay(for: completedAt)
    }
}
