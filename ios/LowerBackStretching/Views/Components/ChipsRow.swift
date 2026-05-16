import SwiftUI

/// Horizontal row of capsule-shaped filter chips. One option is selected at
/// a time. `label` converts an option to user-facing text (e.g. kebab to
/// spaces).
struct ChipsRow: View {
    let options: [String]
    @Binding var selected: String
    var label: (String) -> String = { $0.replacingOccurrences(of: "-", with: " ") }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    let isSelected = selected == option
                    Button { selected = option } label: {
                        Text(label(option))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                Capsule().fill(
                                    isSelected
                                    ? Color.accentColor.opacity(0.2)
                                    : Color(.secondarySystemBackground)
                                )
                            )
                            .foregroundStyle(isSelected ? Color.accentColor : Color.primary)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }
}
