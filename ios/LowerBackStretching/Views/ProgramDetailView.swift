import SwiftUI
import SwiftData

struct ProgramDetailView: View {
    let program: Program
    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Query private var progressRecords: [ProgramProgress]

    struct DayTarget: Hashable {
        let programId: String
        let day: Int
    }

    private var currentDay: Int {
        ProgramProgressService.currentDay(for: program.id, in: progressRecords)
    }

    private var totalDays: Int { program.days.count }
    private var completedProgram: Bool { currentDay > totalDays }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(program.summary).font(.body)

                progressCallout

                ForEach(program.days) { day in
                    NavigationLink(value: DayTarget(programId: program.id, day: day.day)) {
                        InfoRow(
                            title: day.headerTitle + (day.day == currentDay && !completedProgram ? " · Today" : ""),
                            subtitle: day.subtitle(totalSeconds: content.totalDurationSeconds(stretchIds: day.stretchIds))
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(16)
        }
        .navigationTitle(program.title)
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(for: DayTarget.self) { target in
            PlayerView(programId: target.programId, dayNumber: target.day)
        }
    }

    @ViewBuilder
    private var progressCallout: some View {
        if completedProgram {
            HStack {
                Text("Program complete.").font(.headline)
                Spacer()
                Button(role: .destructive) {
                    ProgramProgressService.reset(programId: program.id, in: modelContext)
                } label: {
                    Label("Restart", systemImage: "arrow.counterclockwise")
                }
            }
        } else if currentDay > 1 {
            HStack {
                NavigationLink(value: DayTarget(programId: program.id, day: min(currentDay, totalDays))) {
                    Label("Resume Day \(currentDay)", systemImage: "play.fill")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)

                Button("Start over") {
                    ProgramProgressService.reset(programId: program.id, in: modelContext)
                }
                .buttonStyle(.bordered)
            }
        } else {
            NavigationLink(value: DayTarget(programId: program.id, day: 1)) {
                Label("Start Day 1 of \(totalDays)", systemImage: "play.fill")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
    }
}
