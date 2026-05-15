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
                    let stretches = content.stretches(for: program, day: day.day)
                    let totalSeconds = stretches.reduce(0) { $0 + $1.durationSeconds }
                    NavigationLink(value: DayTarget(programId: program.id, day: day.day)) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Day \(day.day) · \(day.title)").font(.headline)
                            Text("\(day.stretchIds.count) stretches · \(totalSeconds / 60) min")
                                .font(.caption.weight(.medium))
                                .foregroundStyle(.tint)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(RoundedRectangle(cornerRadius: 14).fill(Color(.secondarySystemBackground)))
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
