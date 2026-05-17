import Foundation
import SwiftData

/// Cross-platform parity for [CustomRoutineRepository] on Android.
/// All methods are pure functions that operate on a `ModelContext`;
/// the view layer calls these and lets SwiftData propagate changes via
/// `@Query`.
enum CustomRoutineService {

    /// Insert a copy of [routine] with a " (copy)" name suffix.
    @discardableResult
    static func duplicate(_ routine: CustomRoutine, in context: ModelContext) -> CustomRoutine {
        let copy = CustomRoutine(name: duplicateName(routine.name), stretchIds: routine.stretchIds)
        context.insert(copy)
        return copy
    }

    /// Assigns [ordered] indices 0..n-1 as their `displayOrder`.
    static func reorder(_ ordered: [CustomRoutine]) {
        for (index, routine) in ordered.enumerated() {
            routine.displayOrder = index
        }
    }

    static func softDelete(_ routine: CustomRoutine) {
        routine.deletedAt = .now
    }

    static func restore(_ routine: CustomRoutine) {
        routine.deletedAt = nil
    }
}

func duplicateName(_ original: String) -> String {
    let trimmed = original.trimmingCharacters(in: .whitespacesAndNewlines)
    return trimmed.hasSuffix("(copy)") ? trimmed : "\(trimmed) (copy)"
}
