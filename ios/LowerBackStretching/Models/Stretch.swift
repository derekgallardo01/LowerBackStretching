import Foundation

struct Stretch: Identifiable, Codable, Hashable {
    let id: String
    let name: String
    let bodyParts: [String]
    let durationSeconds: Int
    let difficulty: String
    let description: String
    let youtubeId: String
    /// Optional offset (seconds) at which the YouTube demo should start —
    /// most curated videos have a few seconds of intro before the actual
    /// stretch. When non-nil, the "Watch demo" link includes `?t=` so the
    /// video opens at the right moment. Source of truth: stretches.json.
    var videoStartSeconds: Int? = nil
    /// A one-line summary of why this stretch helps.
    var whyThisStretch: String? = nil
    /// Deeper how-to / anatomy / education cards shown on the detail screen.
    var educationalCards: [EducationalCard]? = nil
    /// Common mistakes the user should watch for.
    var mistakesToAvoid: [String]? = nil
    /// Body-feedback text shown as a small overlay during the player.
    var whatYouShouldFeel: String? = nil
    /// Optional looping stick-figure animation that demonstrates the stretch.
    /// When nil, the player surface shows a placeholder + "Watch demo on
    /// YouTube" link instead.
    var animation: StretchAnimationSpec? = nil
}

/// Looping keyframe animation for a stick-figure renderer.
///
/// The renderer interpolates between consecutive poses using eased segments
/// and wraps back to the first pose after the last — i.e. for poses [A, B]
/// the loop is A → B → A → B …, and for [A, B, C] it is A → B → C → A …
///
/// Each `Pose.joints` entry maps a joint name to its normalized `[x, y]`
/// position in the drawing surface, where `0,0` is top-left and `1,1` is
/// bottom-right. Joint names the renderer understands: `head`, `neck`,
/// `shoulder`, `elbow`, `hand`, `spineMid`, `hip`, `knee`, `foot`. All
/// poses in one spec should declare the same joints.
struct StretchAnimationSpec: Codable, Hashable {
    /// Total duration of one full loop through all poses, in seconds.
    var loopSeconds: Double = 4.0
    let poses: [Pose]
}

struct Pose: Codable, Hashable {
    /// Optional human-readable label (e.g. "cow", "inhale"). Not rendered;
    /// exists for content-author orientation when reading the JSON.
    var name: String? = nil
    let joints: [String: [Double]]
}

struct EducationalCard: Codable, Hashable {
    let title: String
    let body: String
    /// Optional SF Symbol name for visual interest.
    var icon: String? = nil
}

struct GlossaryEntry: Codable, Hashable, Identifiable {
    let term: String
    let definition: String
    /// Free-form category — current values: "anatomy", "concepts".
    let category: String

    var id: String { term }
}
