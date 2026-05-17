import Foundation

/// Slim mirror of the iPhone-side `Stretch`. The watch only needs the
/// id, name, and duration to drive the timer; everything else (video,
/// description, educational cards) stays phone-only.
///
/// Kept duplicated rather than extracted into a shared module so this
/// wave can ship without restructuring the project; the extraction is
/// a natural follow-up.
struct WatchStretch: Codable, Hashable, Identifiable {
    let id: String
    let name: String
    let durationSeconds: Int
}

struct WatchRoutine: Codable, Hashable {
    let name: String
    let stretches: [WatchStretch]
}
