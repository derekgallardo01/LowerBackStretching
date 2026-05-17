import SwiftUI

struct BodyDiagramView: View {
    @EnvironmentObject private var content: ContentStore
    @State private var selectedZone: BodyZone?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                ScreenHeader("Tap where you feel it")
                Text("We'll suggest stretches that target that area.")
                    .font(.body)
                    .foregroundStyle(.secondary)

                HStack {
                    Spacer()
                    BodySilhouette(onZoneTap: { selectedZone = $0 })
                        .frame(width: 220)
                    Spacer()
                }
                .padding(.top, 8)
            }
            .padding(16)
        }
        .navigationTitle("Tap where it hurts")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $selectedZone) { zone in
            ZoneStretchesSheet(zone: zone)
                .presentationDetents([.medium, .large])
        }
    }
}

private struct ZoneStretchesSheet: View {
    let zone: BodyZone
    @EnvironmentObject private var content: ContentStore
    @Environment(\.dismiss) private var dismiss

    private var matches: [Stretch] {
        content.stretches.filter { $0.bodyParts.contains(zone.bodyPartTag) }
    }

    var body: some View {
        NavigationStack {
            Group {
                if matches.isEmpty {
                    VStack {
                        Spacer()
                        Text("No stretches in the catalog target this area yet.")
                            .font(.body)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                        Spacer()
                    }
                } else {
                    List(matches) { stretch in
                        NavigationLink(value: stretch) {
                            InfoRow(
                                title: stretch.name,
                                subtitle: stretch.shortSubtitle()
                            )
                        }
                        .buttonStyle(.plain)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle(zone.displayName)
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(for: Stretch.self) { stretch in
                StretchDetailView(stretch: stretch)
            }
        }
    }
}
