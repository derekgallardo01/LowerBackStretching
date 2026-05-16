import SwiftUI
import SwiftData

struct ProgramsView: View {
    @EnvironmentObject private var content: ContentStore
    @Query(sort: [SortDescriptor(\CustomRoutine.createdAt, order: .reverse)]) private var customRoutines: [CustomRoutine]
    @State private var selectedCategory: String = BodyParts.all
    @State private var showingBuilder: Bool = false

    private var categories: [String] {
        [BodyParts.all] + Array(Set(content.programs.map(\.category))).sorted()
    }

    private var visiblePrograms: [Program] {
        selectedCategory == BodyParts.all
            ? content.programs
            : content.programs.filter { $0.category == selectedCategory }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if !customRoutines.isEmpty {
                    SectionHeader("My routines")
                    ForEach(customRoutines) { routine in
                        NavigationLink(value: routine) {
                            InfoRow(
                                title: routine.name,
                                subtitle: routine.subtitle(totalSeconds: content.totalDurationSeconds(stretchIds: routine.stretchIds))
                            )
                        }
                        .buttonStyle(.plain)
                    }
                    SectionHeader("Built-in programs").padding(.top, 8)
                }

                ChipsRow(options: categories, selected: $selectedCategory)

                ForEach(visiblePrograms) { program in
                    NavigationLink(value: program) {
                        InfoRow(
                            title: program.title,
                            subtitle: program.subtitle,
                            body: program.summary
                        )
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

