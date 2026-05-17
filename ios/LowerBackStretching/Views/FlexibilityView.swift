import SwiftUI
import SwiftData

struct FlexibilityView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: [SortDescriptor(\FlexibilityTest.recordedAt, order: .reverse)])
    private var history: [FlexibilityTest]

    @State private var sitAndReach: String = ""
    @State private var toeTouch: String = ""
    @State private var shoulderReach: String = ""

    private var anyValueEntered: Bool {
        [sitAndReach, toeTouch, shoulderReach].contains { Float($0) != nil }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                ScreenHeader("New measurement")

                VStack(spacing: 10) {
                    measurementField("Sit & reach (cm)", text: $sitAndReach)
                    measurementField("Toe touch (cm past toes)", text: $toeTouch)
                    measurementField("Shoulder reach (cm)", text: $shoulderReach)
                    Button("Save measurement", action: save)
                        .buttonStyle(.borderedProminent)
                        .disabled(!anyValueEntered)
                        .frame(maxWidth: .infinity)
                }
                .padding(16)
                .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))

                if !history.isEmpty {
                    SectionHeader("History")
                    let latest = history.first
                    let previous = history.dropFirst().first
                    let delta = flexibilityDelta(latest: latest, previous: previous)
                    LatestCard(latest: latest!, delta: delta)

                    if history.count > 1 {
                        SectionHeader("Earlier").padding(.top, 4)
                        ForEach(Array(history.dropFirst()), id: \.persistentModelID) { entry in
                            HistoryRow(entry: entry)
                        }
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle("Flexibility self-test")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func measurementField(_ label: String, text: Binding<String>) -> some View {
        TextField(label, text: text)
            .keyboardType(.decimalPad)
            .textFieldStyle(.roundedBorder)
    }

    private func save() {
        FlexibilityService.record(
            sitAndReachCm: Float(sitAndReach),
            toeTouchCm: Float(toeTouch),
            shoulderReachCm: Float(shoulderReach),
            in: modelContext
        )
        sitAndReach = ""
        toeTouch = ""
        shoulderReach = ""
    }
}

private struct LatestCard: View {
    let latest: FlexibilityTest
    let delta: FlexibilityDelta

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(latest.recordedAt.formatted(date: .abbreviated, time: .omitted))
                .font(.headline)
            MeasurementRow(label: "Sit & reach", value: latest.sitAndReachCm, delta: delta.sitAndReachCm)
            MeasurementRow(label: "Toe touch", value: latest.toeTouchCm, delta: delta.toeTouchCm)
            MeasurementRow(label: "Shoulder reach", value: latest.shoulderReachCm, delta: delta.shoulderReachCm)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
}

private struct MeasurementRow: View {
    let label: String
    let value: Float?
    let delta: Float?

    var body: some View {
        HStack {
            Text(label)
            Spacer()
            Text(formatValue())
                .foregroundStyle(deltaColor)
        }
        .font(.body)
    }

    private func formatValue() -> String {
        var s = value.map { String(format: "%.1f cm", $0) } ?? "—"
        if let d = delta { s += "   (\(formatDelta(d)) cm)" }
        return s
    }

    private var deltaColor: Color {
        guard let d = delta else { return .primary }
        return d > 0 ? .accentColor : .primary
    }

    private func formatDelta(_ d: Float) -> String {
        d >= 0 ? String(format: "+%.1f", d) : String(format: "%.1f", d)
    }
}

private struct HistoryRow: View {
    let entry: FlexibilityTest

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(entry.recordedAt.formatted(date: .abbreviated, time: .omitted))
                .font(.subheadline)
            Text(summary())
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 12))
    }

    private func summary() -> String {
        let parts: [String?] = [
            entry.sitAndReachCm.map { String(format: "Sit&reach %.1f", $0) },
            entry.toeTouchCm.map { String(format: "Toes %.1f", $0) },
            entry.shoulderReachCm.map { String(format: "Shoulder %.1f", $0) },
        ]
        let joined = parts.compactMap { $0 }.joined(separator: " · ")
        return joined.isEmpty ? "(no measurements)" : joined
    }
}
