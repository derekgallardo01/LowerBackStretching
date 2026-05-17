import Foundation

struct Stretch: Identifiable, Codable, Hashable {
    let id: String
    let name: String
    let bodyParts: [String]
    let durationSeconds: Int
    let difficulty: String
    let description: String
    let youtubeId: String
    /// A one-line summary of why this stretch helps.
    var whyThisStretch: String? = nil
    /// Deeper how-to / anatomy / education cards shown on the detail screen.
    var educationalCards: [EducationalCard]? = nil
    /// Common mistakes the user should watch for.
    var mistakesToAvoid: [String]? = nil
    /// Body-feedback text shown as a small overlay during the player.
    var whatYouShouldFeel: String? = nil
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
