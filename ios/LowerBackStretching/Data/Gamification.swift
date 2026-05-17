import Foundation

/// Pure calculation functions for the gamification layer. Every
/// function here is total — no I/O, no time-of-day dependency unless
/// passed in explicitly. The view layer combines these with @Query
/// results and renders.
///
/// Cross-platform parity for `Gamification.kt` on Android.

/// XP earned per stretching second. A typical 5-minute routine = 30 XP.
let xpPerSecond: Float = 0.1

/// Total XP required to *reach* a given level. Level 1 is the starting
/// level at 0 XP; the curve adds 100 XP for L2, 200 XP for L3, etc.
///   L1=0, L2=100, L3=300, L4=600, L5=1000, ...
func totalXp(forLevel level: Int) -> Int {
    let l = max(0, level - 1)
    return 50 * l * (l + 1)
}

func xp(forSessionSeconds seconds: Int) -> Int {
    Int(Float(max(0, seconds)) * xpPerSecond)
}

func level(forTotalXp totalXp: Int) -> Int {
    if totalXp <= 0 { return 1 }
    var level = 1
    while self.totalXp(forLevel: level + 1) <= totalXp { level += 1 }
    return level
}

struct XpProgress: Equatable {
    let level: Int
    let totalXp: Int
    let xpIntoLevel: Int
    let xpToNextLevel: Int

    /// 0.0 at the start of the level, 1.0 just before leveling up.
    var progress: Float {
        xpToNextLevel <= 0 ? 1 : Float(xpIntoLevel) / Float(xpToNextLevel)
    }
}

func xpProgress(totalXp: Int) -> XpProgress {
    let safe = max(0, totalXp)
    let lvl = level(forTotalXp: safe)
    let base = self.totalXp(forLevel: lvl)
    let next = self.totalXp(forLevel: lvl + 1)
    return XpProgress(
        level: lvl,
        totalXp: safe,
        xpIntoLevel: safe - base,
        xpToNextLevel: next - base
    )
}

/// Longest run of consecutive completed days, ever. Caller supplies
/// `Set<Date>` of start-of-day values (use `SessionStore.completedDays`).
func longestStreak(days: Set<Date>, calendar: Calendar = .current) -> Int {
    if days.isEmpty { return 0 }
    let sorted = days.sorted()
    var longest = 1
    var current = 1
    for i in 1..<sorted.count {
        if let next = calendar.date(byAdding: .day, value: 1, to: sorted[i - 1]),
           calendar.isDate(next, inSameDayAs: sorted[i]) {
            current += 1
            if current > longest { longest = current }
        } else {
            current = 1
        }
    }
    return longest
}

func weeklyCompletions(days: Set<Date>, today: Date = .now, calendar: Calendar = .current) -> Int {
    let week = calendar.component(.weekOfYear, from: today)
    let year = calendar.component(.yearForWeekOfYear, from: today)
    return days.filter { d in
        calendar.component(.weekOfYear, from: d) == week
            && calendar.component(.yearForWeekOfYear, from: d) == year
    }.count
}

func monthlyCompletions(days: Set<Date>, today: Date = .now, calendar: Calendar = .current) -> Int {
    let month = calendar.component(.month, from: today)
    let year = calendar.component(.year, from: today)
    return days.filter { d in
        calendar.component(.month, from: d) == month
            && calendar.component(.year, from: d) == year
    }.count
}
