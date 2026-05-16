import Foundation

extension Stretch {
    /// "Easy" (capitalized for display from the on-disk "easy").
    var difficultyDisplay: String { difficulty.capitalized }

    /// "30s · Easy · lower back · spine".
    var shortSubtitle: String {
        "\(durationSeconds)s · \(difficultyDisplay) · \(BodyParts.displayList(bodyParts))"
    }
}

extension Array where Element == Stretch {
    /// Filter by a body part. Pass `BodyParts.all` to return everything.
    func filtered(by bodyPart: String) -> [Stretch] {
        bodyPart == BodyParts.all ? self : filter { $0.bodyParts.contains(bodyPart) }
    }
}

extension Program {
    /// "7-day · lower back"
    var subtitle: String {
        "\(days.count)-day · \(category.replacingOccurrences(of: "-", with: " "))"
    }
}

extension ProgramDay {
    /// "Day 1 · Gentle Start"
    var headerTitle: String { "Day \(day) · \(title)" }

    /// "5 stretches · 3 min"
    func subtitle(totalSeconds: Int) -> String {
        "\(stretchIds.count) stretches · \(totalSeconds / 60) min"
    }
}

extension CustomRoutine {
    func subtitle(totalSeconds: Int) -> String {
        "\(stretchIds.count) stretches · \(totalSeconds / 60) min"
    }
}
