import Foundation

/// Synthetic programId values stored on `SessionRecord` for sessions
/// that weren't part of a real `Program` — single-stretch practice and
/// user-created custom routines.
///
/// These strings never round-trip through `ContentStore.program(id:)`
/// (it returns nil, and the UI falls back to showing the raw id). They
/// just need to be unique enough to identify the source.
enum SyntheticProgramId {

    static let singlePrefix = "single-"
    static let routinePrefix = "routine-"

    static func single(_ stretchId: String) -> String { singlePrefix + stretchId }
    static func routine(_ routineId: UUID) -> String { routinePrefix + routineId.uuidString }

    /// Classify a stored programId so consumers (gamification, stats,
    /// etc.) can filter by source without parsing the raw string.
    static func type(for programId: String) -> SessionType {
        if programId.hasPrefix(singlePrefix) { return .single }
        if programId.hasPrefix(routinePrefix) { return .routine }
        return .program
    }
}

enum SessionType: String, CaseIterable {
    case program
    case single
    case routine

    static func fromStorage(_ value: String) -> SessionType {
        SessionType(rawValue: value) ?? .program
    }

    var storageValue: String { rawValue }
}
