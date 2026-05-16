import SwiftUI
import SwiftData

struct HomeView: View {
    @EnvironmentObject private var content: ContentStore
    @Query private var sessions: [SessionRecord]

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var streak: Int { SessionStore.streak(from: completedDays) }
    private var total: Int { sessions.count }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ScreenHeader("Welcome back")

                StreakCard(streak: streak, total: total)

                SectionHeader("Programs").padding(.top, 4)

                ForEach(content.programs) { program in
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
            .padding(16)
        }
        .navigationDestination(for: Program.self) { p in
            ProgramDetailView(program: p)
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct StreakCard: View {
    let streak: Int
    let total: Int

    var body: some View {
        HStack {
            Stat(value: "\(streak)", label: "Day streak")
            Stat(value: "\(total)", label: "Sessions")
        }
        .padding(.vertical, 20)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.accentColor.opacity(0.15))
        )
    }
}
