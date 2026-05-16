import SwiftUI
import SwiftData

struct RoutineBuilderView: View {
    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var name: String = ""
    @State private var filter: String = BodyParts.all
    @State private var selected: Set<String> = []

    private var filterOptions: [String] {
        BodyParts.filterOptions(from: content.stretches)
    }
    private var visible: [Stretch] {
        content.stretches.filtered(by: filter)
    }
    private var totalSeconds: Int {
        content.totalDurationSeconds(stretchIds: Array(selected))
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

            ChipsRow(options: filterOptions, selected: $filter)

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
                            Text(stretch.shortSubtitle)
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
