import Foundation
import SwiftData

enum SessionStore {

    /// Computes the current streak from a list of completion days (start-of-day).
    /// A streak of N means today (or yesterday — grace day) plus N-1 prior days are present.
    static func streak(from completedDays: Set<Date>) -> Int {
        guard !completedDays.isEmpty else { return 0 }
        let cal = Calendar.current
        var cursor = cal.startOfDay(for: .now)
        if !completedDays.contains(cursor) {
            cursor = cal.date(byAdding: .day, value: -1, to: cursor) ?? cursor
        }
        var streak = 0
        while completedDays.contains(cursor) {
            streak += 1
            cursor = cal.date(byAdding: .day, value: -1, to: cursor) ?? cursor
        }
        return streak
    }

    static func completedDays(from sessions: [SessionRecord]) -> Set<Date> {
        Set(sessions.map { $0.completedDay })
    }

    static func record(programId: String, day: Int, durationSeconds: Int, in context: ModelContext) {
        let record = SessionRecord(programId: programId, dayNumber: day, durationSeconds: durationSeconds)
        context.insert(record)
        try? context.save()
    }
}
