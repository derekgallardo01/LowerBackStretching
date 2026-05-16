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
                    NavigationLink(value: DayTarget(programId: program.id, day: day.day)) {
                        InfoRow(
                            title: day.headerTitle,
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
}
