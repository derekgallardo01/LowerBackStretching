import SwiftUI
import SwiftData

struct HomeView: View {
    @EnvironmentObject private var content: ContentStore
    @Query private var sessions: [SessionRecord]
    @AppStorage(SettingsKeys.healthReadEnabled) private var healthReadEnabled: Bool = false
    @AppStorage(SettingsKeys.lastSessionEpochDay) private var lastSessionEpochDay: Int = 0
    @State private var stepsToday: Int?
    @State private var showingCalendar: Bool = false

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var streak: Int { SessionStore.streak(from: completedDays) }
    private var total: Int { sessions.count }
    private var totalSeconds: Int { sessions.reduce(0) { $0 + $1.durationSeconds } }
    private var xpStats: XpProgress { xpProgress(totalXp: xp(forSessionSeconds: totalSeconds)) }
    private var stretchedToday: Bool { lastSessionEpochDay == EpochDay.current() }
    private var showCooldown: Bool {
        shouldShowCooldown(
            enabledRead: healthReadEnabled,
            stretchedToday: stretchedToday,
            stepsToday: stepsToday
        )
    }

    enum Quick: Hashable { case achievements, goals, flexibility, glossary, bodyDiagram, painLog }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ScreenHeader("Welcome back")

                if showCooldown, let steps = stepsToday, let program = content.programs.first {
                    NavigationLink(value: program) {
                        CooldownCard(steps: steps)
                    }
                    .buttonStyle(.plain)
                }

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
                HStack(spacing: 12) {
                    NavigationLink(value: Quick.flexibility) {
                        QuickCard(title: "Flexibility self-test", bodyText: "Track your reach over time")
                    }
                    .buttonStyle(.plain)
                    NavigationLink(value: Quick.glossary) {
                        QuickCard(title: "Glossary", bodyText: "Anatomy & stretching terms")
                    }
                    .buttonStyle(.plain)
                }
                HStack(spacing: 12) {
                    NavigationLink(value: Quick.bodyDiagram) {
                        QuickCard(title: "Tap where it hurts", bodyText: "Find a stretch by body area")
                    }
                    .buttonStyle(.plain)
                    Button { showingCalendar = true } label: {
                        QuickCard(title: "Schedule a break", bodyText: "Add to your calendar")
                    }
                    .buttonStyle(.plain)
                }
                HStack(spacing: 12) {
                    NavigationLink(value: Quick.painLog) {
                        QuickCard(title: "Pain log", bodyText: "Track how your back feels")
                    }
                    .buttonStyle(.plain)
                    // Filler so the card matches the two-column rhythm above.
                    Spacer().frame(maxWidth: .infinity)
                }

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
            case .glossary: GlossaryView()
            case .bodyDiagram: BodyDiagramView()
            case .painLog: PainHistoryView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if healthReadEnabled {
                HealthController.shared.readStepsToday { stepsToday = $0 }
            } else {
                stepsToday = nil
            }
            // Keep the streak-nudge foreground gate in sync with the
            // streak this view just computed.
            StreakNudgeForegroundGate.setStreak(streak)
        }
        .onChange(of: streak) { _, new in
            StreakNudgeForegroundGate.setStreak(new)
        }
        .onChange(of: healthReadEnabled) { _, on in
            if on {
                HealthController.shared.readStepsToday { stepsToday = $0 }
            } else {
                stepsToday = nil
            }
        }
        .sheet(isPresented: $showingCalendar) {
            CalendarEventComposer(
                title: "Stretching break",
                minutesFromNow: 15,
                durationMinutes: 10
            ) {
                showingCalendar = false
            }
        }
    }
}

private struct CooldownCard: View {
    let steps: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Walked \(steps) steps today.").font(.headline)
            Text("Try a quick cooldown stretch to keep your back happy.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
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
