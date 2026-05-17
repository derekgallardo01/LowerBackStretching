import Foundation

/// Per-stretch duration formatted for display.
/// - `.seconds`: "30s"
/// - `.minutesShort`: "0:30" / "1:00" / "1:30"
func formatDuration(_ seconds: Int, unit: DurationUnit) -> String {
    switch unit {
    case .seconds:
        return "\(seconds)s"
    case .minutesShort:
        let m = seconds / 60
        let s = seconds % 60
        return "\(m):\(String(format: "%02d", s))"
    }
}

extension Stretch {
    /// "Easy" (capitalized for display from the on-disk "easy").
    var difficultyDisplay: String { difficulty.capitalized }

    /// "30s · Easy · lower back · spine".
    func shortSubtitle(unit: DurationUnit = .seconds) -> String {
        "\(formatDuration(durationSeconds, unit: unit)) · \(difficultyDisplay) · \(BodyParts.displayList(bodyParts))"
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
