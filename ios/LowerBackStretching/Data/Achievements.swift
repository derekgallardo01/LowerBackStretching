import Foundation

enum AchievementId: String, CaseIterable {
    case firstSession
    case sevenDayStreak
    case thirtyDayStreak
    case fiftySessions
    case hundredSessions
    case levelFive
    case levelTen
}

struct Achievement: Equatable {
    let id: AchievementId
    let title: String
    let description: String
    /// N events required. 1 = binary unlock.
    let target: Int
}

struct AchievementStatus: Equatable {
    let achievement: Achievement
    let progress: Int
    let unlocked: Bool
}

enum Achievements {
    static let all: [Achievement] = [
        Achievement(id: .firstSession,     title: "First steps",   description: "Complete your first stretching session.", target: 1),
        Achievement(id: .sevenDayStreak,   title: "On a roll",     description: "Stretch 7 days in a row.",                target: 7),
        Achievement(id: .thirtyDayStreak,  title: "Habit forged",  description: "Stretch 30 days in a row.",               target: 30),
        Achievement(id: .fiftySessions,    title: "Half a hundred",description: "Complete 50 sessions.",                   target: 50),
        Achievement(id: .hundredSessions,  title: "Centurion",     description: "Complete 100 sessions.",                  target: 100),
        Achievement(id: .levelFive,        title: "Limber",        description: "Reach level 5.",                          target: 5),
        Achievement(id: .levelTen,         title: "Bendy",         description: "Reach level 10.",                         target: 10),
    ]
}

/// Compute the status of every achievement from already-derived stats
/// (no DB access). Locked rows still carry a `progress` value so the UI
/// can render a "23 / 50" indicator under each badge.
func evaluateAchievements(
    totalSessions: Int,
    longestStreak: Int,
    level: Int
) -> [AchievementStatus] {
    Achievements.all.map { a in
        let raw: Int
        switch a.id {
        case .firstSession, .fiftySessions, .hundredSessions:
            raw = totalSessions
        case .sevenDayStreak, .thirtyDayStreak:
            raw = longestStreak
        case .levelFive, .levelTen:
            raw = level
        }
        return AchievementStatus(
            achievement: a,
            progress: min(raw, a.target),
            unlocked: raw >= a.target
        )
    }
}
