import Foundation
import SwiftData

enum SessionStore {

    /// Computes the current streak from a list of completion days (start-of-day).
    /// A streak of N means today (or yesterday — grace day) plus N-1 prior days are present.
    static func streak(from completedDays: Set<Date>, today: Date = .now, calendar: Calendar = .current) -> Int {
        guard !completedDays.isEmpty else { return 0 }
        var cursor = calendar.startOfDay(for: today)
        if !completedDays.contains(cursor) {
            cursor = calendar.date(byAdding: .day, value: -1, to: cursor) ?? cursor
        }
        var streak = 0
        while completedDays.contains(cursor) {
            streak += 1
            cursor = calendar.date(byAdding: .day, value: -1, to: cursor) ?? cursor
        }
        return streak
    }

    static func completedDays(from sessions: [SessionRecord]) -> Set<Date> {
        Set(sessions.map { $0.completedDay })
    }

    static func record(programId: String, day: Int, durationSeconds: Int, in context: ModelContext) {
        let record = SessionRecord(
            programId: programId,
            dayNumber: day,
            durationSeconds: durationSeconds,
            type: SyntheticProgramId.type(for: programId)
        )
        context.insert(record)
        try? context.save()
        UserDefaults.standard.set(EpochDay.current(), forKey: SettingsKeys.lastSessionEpochDay)
        ReminderManager.clearDelivered()
        StreakNudgeManager.clearDelivered()

        // Mirror to the cloud off the player's path (no-op unless the Firebase
        // backend is configured and the sync toggle is on).
        let completedAtMillis = Int64(record.completedAt.timeIntervalSince1970 * 1000)
        let sessionType = record.type
        Task { @MainActor in
            await SyncController.shared.pushSessionIfEnabled(
                programId: programId,
                dayNumber: day,
                durationSeconds: durationSeconds,
                completedAtEpochMillis: completedAtMillis,
                type: sessionType
            )
        }
    }
}
