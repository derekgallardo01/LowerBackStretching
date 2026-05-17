import Foundation

/// Persistent record of a routine the user started but didn't finish.
/// At most one is stored at a time — starting a different routine
/// overwrites it. The view writes on every index change and clears on
/// completion. The player reads it on init to resume where the user
/// left off.
struct InProgressSession: Equatable {
    let programId: String
    let dayNumber: Int
    let index: Int
}

enum InProgressKeys {
    static let programId = "in_progress_program_id"
    static let dayNumber = "in_progress_day"
    static let index = "in_progress_index"
}

enum InProgressStore {

    static func load(from defaults: UserDefaults = .standard) -> InProgressSession? {
        guard let pid = defaults.string(forKey: InProgressKeys.programId) else { return nil }
        return InProgressSession(
            programId: pid,
            dayNumber: defaults.integer(forKey: InProgressKeys.dayNumber),
            index: defaults.integer(forKey: InProgressKeys.index)
        )
    }

    static func save(_ session: InProgressSession, in defaults: UserDefaults = .standard) {
        defaults.set(session.programId, forKey: InProgressKeys.programId)
        defaults.set(session.dayNumber, forKey: InProgressKeys.dayNumber)
        defaults.set(session.index, forKey: InProgressKeys.index)
    }

    static func clear(from defaults: UserDefaults = .standard) {
        defaults.removeObject(forKey: InProgressKeys.programId)
        defaults.removeObject(forKey: InProgressKeys.dayNumber)
        defaults.removeObject(forKey: InProgressKeys.index)
    }

    /// Returns the saved index if it matches this source, else 0.
    static func resumeIndex(
        for programId: String,
        dayNumber: Int,
        defaults: UserDefaults = .standard
    ) -> Int {
        guard let saved = load(from: defaults),
              saved.programId == programId,
              saved.dayNumber == dayNumber
        else { return 0 }
        return saved.index
    }
}
