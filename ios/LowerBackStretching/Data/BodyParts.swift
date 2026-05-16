import Foundation

/// Helpers for the `bodyParts: [String]` field on `Stretch`. Strings on disk
/// are kebab-case (`lower-back`); humans see them as `lower back`.
enum BodyParts {

    static let all = "all"

    /// "lower-back" -> "lower back"
    static func display(_ part: String) -> String {
        part.replacingOccurrences(of: "-", with: " ")
    }

    static func displayList(_ parts: [String], separator: String = " · ") -> String {
        parts.map { display($0) }.joined(separator: separator)
    }

    /// All unique body parts present in the catalog, sorted.
    static func distinctSorted(from stretches: [Stretch]) -> [String] {
        Array(Set(stretches.flatMap(\.bodyParts))).sorted()
    }

    /// Same as `distinctSorted` but with `"all"` prepended for filter UIs.
    static func filterOptions(from stretches: [Stretch]) -> [String] {
        [all] + distinctSorted(from: stretches)
    }
}
