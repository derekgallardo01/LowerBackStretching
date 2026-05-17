import SwiftUI

/// Shown when a deep link with a shared routine opens the app. Lets the
/// user preview the routine and decide whether to add it to their
/// library. Unknown stretch ids in the import are dropped on save.
struct ImportRoutineSheet: View {
    let routine: SharedRoutine
    /// Called with the resolved list of known stretch ids when the user
    /// confirms, or with an empty list if they cancel.
    let onResolve: ([String]) -> Void

    @EnvironmentObject private var content: ContentStore

    private var knownIds: [String] {
        routine.stretchIds.filter { content.stretch(id: $0) != nil }
    }
    private var unknownCount: Int { routine.stretchIds.count - knownIds.count }

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 10) {
                Text("Import routine").font(.title2.weight(.semibold))
                Text(routine.name).font(.headline)
                Text(summary)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                ScrollView {
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(knownIds, id: \.self) { id in
                            if let s = content.stretch(id: id) {
                                Text("• \(s.name)").font(.body)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .frame(maxHeight: 200)

                HStack {
                    Button("Cancel", role: .cancel) { onResolve([]) }
                        .frame(maxWidth: .infinity)
                        .buttonStyle(.bordered)
                    Button("Add to my routines") { onResolve(knownIds) }
                        .frame(maxWidth: .infinity)
                        .buttonStyle(.borderedProminent)
                        .disabled(knownIds.isEmpty)
                }
                .padding(.top, 8)
            }
            .padding(16)
        }
    }

    private var summary: String {
        var s = "\(knownIds.count) stretches"
        if unknownCount > 0 {
            s += " (skipped \(unknownCount) unknown id\(unknownCount > 1 ? "s" : ""))"
        }
        return s
    }
}
