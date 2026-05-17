import Foundation
import SwiftData

/// Per-program bookmark of the next day the user should do. Inserted on
/// first completion of any day in the program; updated on each
/// subsequent completion. Synthetic program ids (single-* / routine-*)
/// never write to this store — only canned programs do.
@Model
final class ProgramProgress {
    @Attribute(.unique) var programId: String
    var currentDay: Int
    var updatedAt: Date

    init(programId: String, currentDay: Int) {
        self.programId = programId
        self.currentDay = currentDay
        self.updatedAt = .now
    }
}
