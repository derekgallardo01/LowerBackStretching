import SwiftUI
import SwiftData

struct RoutineBuilderView: View {
    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var name: String = ""
    @State private var filter: String = "all"
    @State private var selected: Set<String> = []

    private var bodyParts: [String] {
        ["all"] + Array(Set(content.stretches.flatMap(\.bodyParts))).sorted()
    }
    private var visible: [Stretch] {
        filter == "all"
            ? content.stretches
            : content.stretches.filter { $0.bodyParts.contains(filter) }
    }
    private var totalSeconds: Int {
        selected.compactMap { content.stretch(id: $0)?.durationSeconds }.reduce(0, +)
    }
    private var canSave: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty && !selected.isEmpty
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Routine name", text: $name)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal, 16).padding(.top, 16)

            Text("\(selected.count) stretches selected · \(totalSeconds / 60) min")
                .font(.caption.weight(.medium))
                .padding(.horizontal, 16)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(bodyParts, id: \.self) { part in
                        Button { filter = part } label: {
                            Text(part.replacingOccurrences(of: "-", with: " "))
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(
                                    Capsule().fill(
                                        filter == part
                                        ? Color.accentColor.opacity(0.2)
                                        : Color(.secondarySystemBackground)
                                    )
                                )
                                .foregroundStyle(filter == part ? Color.accentColor : Color.primary)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            List(visible) { stretch in
                Button {
                    if selected.contains(stretch.id) {
                        selected.remove(stretch.id)
                    } else {
                        selected.insert(stretch.id)
                    }
                } label: {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(stretch.name).foregroundStyle(.primary)
                            Text("\(stretch.durationSeconds)s · \(stretch.bodyParts.map { $0.replacingOccurrences(of: "-", with: " ") }.joined(separator: " · "))")
                                .font(.caption)
                                .foregroundStyle(.tint)
                        }
                        Spacer()
                        if selected.contains(stretch.id) {
                            Image(systemName: "checkmark").foregroundStyle(.tint)
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("New routine")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") {
                    let routine = CustomRoutine(name: name, stretchIds: Array(selected))
                    modelContext.insert(routine)
                    try? modelContext.save()
                    dismiss()
                }
                .disabled(!canSave)
            }
        }
    }
}
