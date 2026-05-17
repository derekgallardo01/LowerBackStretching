import SwiftUI
import SwiftData

struct AchievementsView: View {
    @Query private var sessions: [SessionRecord]

    private var totalSessions: Int { sessions.count }
    private var totalSeconds: Int { sessions.reduce(0) { $0 + $1.durationSeconds } }
    private var longest: Int {
        longestStreak(days: SessionStore.completedDays(from: sessions))
    }
    private var currentLevel: Int { level(forTotalXp: xp(forSessionSeconds: totalSeconds)) }

    private var statuses: [AchievementStatus] {
        evaluateAchievements(
            totalSessions: totalSessions,
            longestStreak: longest,
            level: currentLevel
        )
    }

    var body: some View {
        List(statuses, id: \.achievement.id) { status in
            AchievementRow(status: status)
                .listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .navigationTitle("Achievements")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct AchievementRow: View {
    let status: AchievementStatus

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            Badge(unlocked: status.unlocked)
            VStack(alignment: .leading, spacing: 4) {
                Text(status.achievement.title).font(.headline)
                Text(status.achievement.description).font(.caption).foregroundStyle(.secondary)
                if !status.unlocked {
                    ProgressView(value: Double(status.progress), total: Double(status.achievement.target))
                        .padding(.top, 6)
                    Text("\(status.progress) / \(status.achievement.target)")
                        .font(.caption2)
                }
            }
        }
        .padding(.vertical, 8)
    }
}

private struct Badge: View {
    let unlocked: Bool
    var body: some View {
        ZStack {
            Circle()
                .fill(unlocked ? Color.accentColor.opacity(0.2) : Color.gray.opacity(0.15))
                .frame(width: 56, height: 56)
            Image(systemName: unlocked ? "trophy.fill" : "lock.fill")
                .foregroundStyle(unlocked ? Color.accentColor : .gray)
        }
    }
}
