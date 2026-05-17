import SwiftUI

/// Renders 1–3 filled dots for a difficulty string. Unknown values render
/// one dot. Accessibility label still reads the difficulty name so
/// VoiceOver doesn't lose the information.
struct DifficultyDots: View {
    let difficulty: String

    private var filled: Int {
        switch difficulty.lowercased() {
        case "easy":   return 1
        case "medium": return 2
        case "hard":   return 3
        default:       return 1
        }
    }

    var body: some View {
        HStack(spacing: 3) {
            ForEach(0..<3, id: \.self) { i in
                Circle()
                    .fill(i < filled ? Color.accentColor : Color.primary.opacity(0.2))
                    .overlay(
                        Circle().stroke(
                            i < filled ? Color.clear : Color.primary.opacity(0.35),
                            lineWidth: 1
                        )
                    )
                    .frame(width: 8, height: 8)
            }
        }
        .accessibilityElement()
        .accessibilityLabel(Text(difficulty.capitalized))
    }
}
