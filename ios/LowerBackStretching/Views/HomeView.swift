import SwiftUI
import SwiftData

struct HomeView: View {
    @EnvironmentObject private var content: ContentStore
    @Query private var sessions: [SessionRecord]

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var streak: Int { SessionStore.streak(from: completedDays) }
    private var total: Int { sessions.count }
    private var totalSeconds: Int { sessions.reduce(0) { $0 + $1.durationSeconds } }
    private var xpStats: XpProgress { xpProgress(totalXp: xp(forSessionSeconds: totalSeconds)) }

    enum Quick: Hashable { case achievements, goals, flexibility }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ScreenHeader("Welcome back")

                StatsCard(streak: streak, total: total, xp: xpStats)

                HStack(spacing: 12) {
                    NavigationLink(value: Quick.goals) {
                        QuickCard(title: "Goals", bodyText: "Weekly & monthly targets")
                    }
                    .buttonStyle(.plain)
                    NavigationLink(value: Quick.achievements) {
                        QuickCard(title: "Achievements", bodyText: "Badges & milestones")
                    }
                    .buttonStyle(.plain)
                }
                NavigationLink(value: Quick.flexibility) {
                    QuickCard(title: "Flexibility self-test", bodyText: "Track your reach over time")
                }
                .buttonStyle(.plain)

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
        .navigationDestination(for: Quick.self) { quick in
            switch quick {
            case .achievements: AchievementsView()
            case .goals: GoalsView()
            case .flexibility: FlexibilityView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct StatsCard: View {
    let streak: Int
    let total: Int
    let xp: XpProgress

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Stat(value: "\(streak)", label: "Day streak")
                Stat(value: "\(total)", label: "Sessions")
                Stat(value: "L\(xp.level)", label: "Level")
            }
            ProgressView(value: Double(xp.progress.isFinite ? xp.progress : 0))
            Text("\(xp.xpIntoLevel) / \(xp.xpToNextLevel) XP to next level")
                .font(.caption)
        }
        .padding(.vertical, 20)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.accentColor.opacity(0.15))
        )
    }
}

private struct QuickCard: View {
    let title: String
    let bodyText: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(.headline)
            Text(bodyText).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
}
