import Foundation
import SwiftData

/// Cross-platform parity for `ProgramProgressRepository` on Android.
/// Reads/writes the [ProgramProgress] table; no-ops for synthetic
/// program ids (single-* / routine-*) so only canned programs build
/// a per-day bookmark.
enum ProgramProgressService {

    /// The next day the user should do for [programId]. Returns 1 if
    /// the program has never been started.
    static func currentDay(for programId: String, in records: [ProgramProgress]) -> Int {
        records.first(where: { $0.programId == programId })?.currentDay ?? 1
    }

    /// Advance the bookmark after a completion. Caps at totalDays + 1
    /// so the caller can detect "program done" by reading > totalDays.
    static func advance(
        programId: String,
        completedDay: Int,
        totalDays: Int,
        in context: ModelContext
    ) {
        guard SyntheticProgramId.type(for: programId) == .program else { return }
        let next = min(completedDay + 1, totalDays + 1)

        if let existing = lookup(programId: programId, in: context) {
            existing.currentDay = next
            existing.updatedAt = .now
        } else {
            context.insert(ProgramProgress(programId: programId, currentDay: next))
        }
        try? context.save()
    }

    static func reset(programId: String, in context: ModelContext) {
        guard let existing = lookup(programId: programId, in: context) else { return }
        context.delete(existing)
        try? context.save()
    }

    private static func lookup(programId: String, in context: ModelContext) -> ProgramProgress? {
        let predicate = #Predicate<ProgramProgress> { $0.programId == programId }
        let descriptor = FetchDescriptor<ProgramProgress>(predicate: predicate)
        return try? context.fetch(descriptor).first
    }
}
