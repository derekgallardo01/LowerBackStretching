import Foundation

/// Synthetic programId values stored on `SessionRecord` for sessions that
/// weren't part of a real `Program` — single-stretch practice and
/// user-created custom routines.
///
/// These strings never round-trip through `ContentStore.program(id:)`
/// (it returns nil, and the UI falls back to showing the raw id). They
/// just need to be unique enough to identify the source.
enum SyntheticProgramId {
    static func single(_ stretchId: String) -> String { "single-\(stretchId)" }
    static func routine(_ routineId: UUID) -> String { "routine-\(routineId.uuidString)" }
}
