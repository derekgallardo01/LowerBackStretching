import SwiftUI
import SwiftData

struct ProgramsView: View {
    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Query(
        filter: #Predicate<CustomRoutine> { $0.deletedAt == nil },
        sort: [
            SortDescriptor(\CustomRoutine.displayOrder, order: .forward),
            SortDescriptor(\CustomRoutine.createdAt, order: .reverse),
        ]
    ) private var customRoutines: [CustomRoutine]

    @State private var selectedCategory: String = BodyParts.all
    @State private var showingBuilder: Bool = false
    @State private var pendingDelete: CustomRoutine?
    @State private var showUndo: Bool = false
    @State private var sharingRoutine: CustomRoutine?

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
                        routineRow(routine)
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
        .overlay(alignment: .bottom) { undoBanner }
        .sheet(item: $sharingRoutine) { routine in
            NavigationStack { ShareRoutineView(routine: routine) }
        }
    }

    @ViewBuilder
    private func routineRow(_ routine: CustomRoutine) -> some View {
        NavigationLink(value: routine) {
            InfoRow(
                title: routine.name,
                subtitle: stretchCountSubtitle(
                    stretchCount: routine.stretchIds.count,
                    totalSeconds: content.totalDurationSeconds(stretchIds: routine.stretchIds)
                )
            )
        }
        .buttonStyle(.plain)
        .contextMenu {
            Button {
                sharingRoutine = routine
            } label: {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            Button {
                CustomRoutineService.duplicate(routine, in: modelContext)
            } label: {
                Label("Duplicate", systemImage: "doc.on.doc")
            }
            if let index = customRoutines.firstIndex(where: { $0.id == routine.id }), index > 0 {
                Button {
                    moveRoutine(routine, by: -1)
                } label: {
                    Label("Move up", systemImage: "arrow.up")
                }
            }
            if let index = customRoutines.firstIndex(where: { $0.id == routine.id }),
               index < customRoutines.count - 1 {
                Button {
                    moveRoutine(routine, by: 1)
                } label: {
                    Label("Move down", systemImage: "arrow.down")
                }
            }
            Button(role: .destructive) {
                softDelete(routine)
            } label: {
                Label("Delete", systemImage: "trash")
            }
        }
    }

    @ViewBuilder
    private var undoBanner: some View {
        if showUndo, let target = pendingDelete {
            HStack {
                Text("Deleted \(target.name)").font(.subheadline)
                Spacer()
                Button("Undo") {
                    CustomRoutineService.restore(target)
                    pendingDelete = nil
                    showUndo = false
                }
                .buttonStyle(.bordered)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal, 16)
            .padding(.bottom, 20)
            .transition(.move(edge: .bottom).combined(with: .opacity))
        }
    }

    private func moveRoutine(_ routine: CustomRoutine, by delta: Int) {
        guard let index = customRoutines.firstIndex(where: { $0.id == routine.id }) else { return }
        let target = index + delta
        guard target >= 0, target < customRoutines.count else { return }
        var reordered = customRoutines
        reordered.remove(at: index)
        reordered.insert(routine, at: target)
        CustomRoutineService.reorder(reordered)
    }

    private func softDelete(_ routine: CustomRoutine) {
        CustomRoutineService.softDelete(routine)
        pendingDelete = routine
        withAnimation { showUndo = true }
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
            if pendingDelete?.id == routine.id {
                withAnimation { showUndo = false }
                pendingDelete = nil
            }
        }
    }
}
