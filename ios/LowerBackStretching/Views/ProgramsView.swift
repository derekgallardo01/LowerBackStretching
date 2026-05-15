import SwiftUI
import SwiftData

struct ProgramsView: View {
    @EnvironmentObject private var content: ContentStore
    @Query(sort: [SortDescriptor(\CustomRoutine.createdAt, order: .reverse)]) private var customRoutines: [CustomRoutine]
    @State private var selectedCategory: String = "all"
    @State private var showingBuilder: Bool = false

    private var categories: [String] {
        ["all"] + Array(Set(content.programs.map(\.category))).sorted()
    }
    private var visible: [Program] {
        selectedCategory == "all" ? content.programs : content.programs.filter { $0.category == selectedCategory }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if !customRoutines.isEmpty {
                    Text("My routines").font(.headline)
                    ForEach(customRoutines) { routine in
                        NavigationLink(value: routine) {
                            CustomRoutineRow(routine: routine, content: content)
                        }
                        .buttonStyle(.plain)
                    }

                    Text("Built-in programs")
                        .font(.headline)
                        .padding(.top, 8)
                }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { c in
                            Button { selectedCategory = c } label: {
                                Text(c.replacingOccurrences(of: "-", with: " "))
                                    .padding(.horizontal, 12).padding(.vertical, 6)
                                    .background(
                                        Capsule().fill(
                                            selectedCategory == c
                                            ? Color.accentColor.opacity(0.2)
                                            : Color(.secondarySystemBackground)
                                        )
                                    )
                                    .foregroundStyle(selectedCategory == c ? Color.accentColor : Color.primary)
                            }
                        }
                    }
                }

                ForEach(visible) { program in
                    NavigationLink(value: program) {
                        ProgramCardView(program: program)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
        .navigationTitle("Programs")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: { showingBuilder = true }) {
                    Label("New routine", systemImage: "plus")
                }
            }
        }
        .sheet(isPresented: $showingBuilder) {
            NavigationStack { RoutineBuilderView() }
        }
        .navigationDestination(for: Program.self) { p in
            ProgramDetailView(program: p)
        }
        .navigationDestination(for: CustomRoutine.self) { r in
            CustomRoutinePlayerView(routine: r)
        }
    }
}

private struct CustomRoutineRow: View {
    let routine: CustomRoutine
    let content: ContentStore

    var body: some View {
        let totalSeconds = routine.stretchIds.compactMap { content.stretch(id: $0)?.durationSeconds }.reduce(0, +)
        VStack(alignment: .leading, spacing: 4) {
            Text(routine.name).font(.title3.weight(.semibold))
            Text("\(routine.stretchIds.count) stretches · \(totalSeconds / 60) min")
                .font(.caption.weight(.medium))
                .foregroundStyle(.tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
    }
}
