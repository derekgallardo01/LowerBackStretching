import SwiftUI

/// Pre/post-session pain rating prompt. Mirrors the Android
/// `PainCheckInDialog` — a 0..10 slider plus optional body-zone selection
/// via the same back-view silhouette used elsewhere.
///
/// Hosted as a `.sheet` from the player so the user can dismiss with a
/// downward swipe (treated as Skip).
struct PainCheckInSheet: View {
    let title: String
    let onSubmit: (_ painLevel: Int, _ bodyLocationTag: String?) -> Void
    let onSkip: () -> Void

    @State private var level: Int = 3
    @State private var selectedZone: BodyZone? = nil
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("0 = no pain · 10 = severe")
                        .font(.caption)
                        .foregroundStyle(.secondary)

                    VStack(spacing: 4) {
                        Slider(
                            value: Binding(
                                get: { Double(level) },
                                set: { level = Int($0.rounded()) }
                            ),
                            in: 0...10,
                            step: 1
                        )
                        .accessibilityLabel("Pain level")
                        .accessibilityValue("\(level)")

                        HStack {
                            Text("0").font(.caption2).foregroundStyle(.secondary)
                            Spacer()
                            Text("\(level)")
                                .font(.title.weight(.semibold))
                                .foregroundStyle(.tint)
                                .accessibilityIdentifier("painLevelValue")
                            Spacer()
                            Text("10").font(.caption2).foregroundStyle(.secondary)
                        }
                    }

                    Text("Where (optional)")
                        .font(.subheadline.weight(.medium))

                    HStack(alignment: .top, spacing: 16) {
                        BodySilhouette(
                            onZoneTap: { zone in
                                selectedZone = selectedZone == zone ? nil : zone
                            },
                            highlightedZones: selectedZone.map { Set([$0]) } ?? []
                        )
                        .frame(width: 120)

                        Text(selectedZone?.displayName ?? "Tap a zone (or skip)")
                            .font(.body)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(Color.secondary.opacity(0.15))
                            )
                    }
                }
                .padding(20)
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Skip") {
                        onSkip()
                        dismiss()
                    }
                    .accessibilityIdentifier("painCheckInSkip")
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSubmit(level, selectedZone?.bodyPartTag)
                        dismiss()
                    }
                    .accessibilityIdentifier("painCheckInSave")
                }
            }
            .interactiveDismissDisabled(false)
        }
        .presentationDetents([.medium, .large])
    }
}
