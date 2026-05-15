import SwiftUI

struct StretchesView: View {
    @EnvironmentObject private var content: ContentStore
    @State private var selectedPart: String = "all"

    private var bodyParts: [String] {
        ["all"] + Array(Set(content.stretches.flatMap(\.bodyParts))).sorted()
    }

    private var visible: [Stretch] {
        selectedPart == "all"
            ? content.stretches
            : content.stretches.filter { $0.bodyParts.contains(selectedPart) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(bodyParts, id: \.self) { part in
                        Button { selectedPart = part } label: {
                            Text(part.replacingOccurrences(of: "-", with: " "))
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(
                                    Capsule().fill(
                                        selectedPart == part
                                        ? Color.accentColor.opacity(0.2)
                                        : Color(.secondarySystemBackground)
                                    )
                                )
                                .foregroundStyle(selectedPart == part ? Color.accentColor : Color.primary)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            ScrollView {
                VStack(spacing: 10) {
                    ForEach(visible) { stretch in
                        NavigationLink(value: stretch) {
                            StretchRow(stretch: stretch)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
        }
        .navigationTitle("Stretches")
        .navigationDestination(for: Stretch.self) { s in
            StretchDetailView(stretch: s)
        }
    }
}

private struct StretchRow: View {
    let stretch: Stretch
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(stretch.name).font(.headline)
            Text("\(stretch.durationSeconds)s · \(stretch.difficulty) · \(stretch.bodyParts.map { $0.replacingOccurrences(of: "-", with: " ") }.joined(separator: " · "))")
                .font(.caption.weight(.medium))
                .foregroundStyle(.tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color(.secondarySystemBackground)))
    }
}
