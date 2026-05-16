import SwiftUI

struct ProgramDetailView: View {
    let program: Program
    @EnvironmentObject private var content: ContentStore

    struct DayTarget: Hashable {
        let programId: String
        let day: Int
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(program.summary).font(.body)

                ForEach(program.days) { day in
                    let totalSeconds = content.stretches(for: program, day: day.day)
                        .reduce(0) { $0 + $1.durationSeconds }
                    NavigationLink(value: DayTarget(programId: program.id, day: day.day)) {
                        InfoRow(
                            title: day.headerTitle,
                            subtitle: day.subtitle(totalSeconds: totalSeconds),
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
}
