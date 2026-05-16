import SwiftUI
import SwiftData

struct CalendarView: View {
    @Query(sort: [SortDescriptor(\SessionRecord.completedAt, order: .reverse)]) private var sessions: [SessionRecord]
    @State private var displayedMonth: Date = Calendar.current.startOfMonth(for: .now)

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var streak: Int { SessionStore.streak(from: completedDays) }
    private var recent: [SessionRecord] { Array(sessions.prefix(20)) }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HStack {
                    Stat(value: "\(streak)", label: "Streak")
                    Stat(value: "\(sessions.count)", label: "Sessions")
                    Stat(value: "\(completedDays.count)", label: "Active days")
                }

                MonthCard(month: $displayedMonth, completed: completedDays)

                RecentSessions(sessions: recent)
            }
            .padding(16)
        }
        .navigationTitle("Calendar")
    }
}

private struct RecentSessions: View {
    let sessions: [SessionRecord]
    @EnvironmentObject private var content: ContentStore

    var body: some View {
        if sessions.isEmpty {
            Text("No sessions yet. Start a routine to track your progress.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
        } else {
            VStack(alignment: .leading, spacing: 8) {
                SectionHeader("Recent sessions")
                ForEach(sessions) { session in
                    InfoRow(
                        title: "\(programTitle(session)) · Day \(session.dayNumber)",
                        subtitle: "\(session.completedAt.formatted(date: .abbreviated, time: .shortened)) · \(session.durationSeconds / 60) min",
                    )
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private func programTitle(_ session: SessionRecord) -> String {
        content.program(id: session.programId)?.title ?? session.programId
    }
}

private struct MonthCard: View {
    @Binding var month: Date
    let completed: Set<Date>
    private let cal = Calendar.current

    var body: some View {
        let grid = CalendarMonth(month: month, calendar: cal)
        VStack(spacing: 12) {
            MonthHeader(month: $month)

            HStack {
                ForEach(grid.weekdaySymbols, id: \.self) { symbol in
                    Text(symbol)
                        .frame(maxWidth: .infinity)
                        .font(.caption.weight(.medium))
                        .foregroundStyle(.secondary)
                }
            }

            ForEach(grid.weeks, id: \.self) { week in
                HStack(spacing: 4) {
                    ForEach(week, id: \.self) { day in
                        DayCell(
                            date: day,
                            isInMonth: grid.isInMonth(day),
                            done: completed.contains(cal.startOfDay(for: day)),
                        )
                    }
                }
            }
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
    }
}

private struct MonthHeader: View {
    @Binding var month: Date
    private let cal = Calendar.current

    var body: some View {
        HStack {
            Button(action: { month = cal.date(byAdding: .month, value: -1, to: month) ?? month }) {
                Image(systemName: "chevron.left").font(.title3)
            }
            Spacer()
            Text(month.formatted(.dateTime.month(.wide).year()))
                .font(.title3.weight(.semibold))
            Spacer()
            Button(action: { month = cal.date(byAdding: .month, value: 1, to: month) ?? month }) {
                Image(systemName: "chevron.right").font(.title3)
            }
        }
    }
}

private struct DayCell: View {
    let date: Date
    let isInMonth: Bool
    let done: Bool

    var body: some View {
        let today = Calendar.current.isDateInToday(date)
        Text("\(Calendar.current.component(.day, from: date))")
            .font(.callout.weight(today ? .bold : .regular))
            .frame(maxWidth: .infinity, minHeight: 36)
            .background(
                Circle()
                    .fill(done ? Color.accentColor : (today ? Color.accentColor.opacity(0.2) : Color.clear))
                    .frame(width: 32, height: 32)
            )
            .foregroundStyle(
                done ? Color.white
                : (isInMonth ? Color.primary : Color.secondary.opacity(0.4))
            )
    }
}
