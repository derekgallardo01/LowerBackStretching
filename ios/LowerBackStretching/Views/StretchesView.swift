import SwiftUI

struct StretchesView: View {
    @EnvironmentObject private var content: ContentStore
    @State private var filter: String = BodyParts.all
    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    private var unit: DurationUnit { DurationUnit.fromStorage(durationUnitRaw) }

    private var filterOptions: [String] {
        BodyParts.filterOptions(from: content.stretches)
    }

    private var visible: [Stretch] {
        content.stretches.filtered(by: filter)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ChipsRow(options: filterOptions, selected: $filter)

            ScrollView {
                VStack(spacing: 10) {
                    ForEach(visible) { stretch in
                        NavigationLink(value: stretch) {
                            InfoRow(title: stretch.name, subtitle: stretch.shortSubtitle(unit: unit))
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
