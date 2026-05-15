import SwiftUI
import SwiftData

struct CalendarView: View {
    @Query private var sessions: [SessionRecord]
    @State private var displayedMonth: Date = Calendar.current.startOfMonth(for: .now)

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var streak: Int { SessionStore.streak(from: completedDays) }
    private var total: Int { sessions.count }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HStack {
                    Stat(value: "\(streak)", label: "Streak")
                    Stat(value: "\(total)", label: "Sessions")
                    Stat(value: "\(completedDays.count)", label: "Active days")
                }
                .frame(maxWidth: .infinity)

                MonthCard(
                    month: $displayedMonth,
                    completed: completedDays,
                )
            }
            .padding(16)
        }
        .navigationTitle("Calendar")
    }
}

private struct Stat: View {
    let value: String
    let label: String
    var body: some View {
        VStack(spacing: 4) {
            Text(value).font(.title.weight(.semibold))
            Text(label).font(.caption.weight(.medium)).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct MonthCard: View {
    @Binding var month: Date
    let completed: Set<Date>
    private let cal = Calendar.current

    var body: some View {
        VStack(spacing: 12) {
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

            HStack {
                ForEach(weekdaySymbols(), id: \.self) { symbol in
                    Text(symbol)
                        .frame(maxWidth: .infinity)
                        .font(.caption.weight(.medium))
                        .foregroundStyle(.secondary)
                }
            }

            ForEach(weeks(), id: \.self) { week in
                HStack(spacing: 4) {
                    ForEach(week, id: \.self) { day in
                        DayCell(date: day, isInMonth: cal.isDate(day, equalTo: month, toGranularity: .month), done: completed.contains(cal.startOfDay(for: day)))
                    }
                }
            }
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
    }

    private func weekdaySymbols() -> [String] {
        var symbols = cal.veryShortStandaloneWeekdaySymbols
        let firstWeekday = cal.firstWeekday - 1
        symbols = Array(symbols[firstWeekday...]) + Array(symbols[..<firstWeekday])
        return symbols
    }

    private func weeks() -> [[Date]] {
        let firstOfMonth = cal.startOfMonth(for: month)
        let range = cal.range(of: .day, in: .month, for: firstOfMonth) ?? 1..<2
        let firstWeekdayOffset = (cal.component(.weekday, from: firstOfMonth) - cal.firstWeekday + 7) % 7
        let start = cal.date(byAdding: .day, value: -firstWeekdayOffset, to: firstOfMonth)!
        let totalCells = ((firstWeekdayOffset + range.count + 6) / 7) * 7

        var days: [Date] = []
        for i in 0..<totalCells {
            days.append(cal.date(byAdding: .day, value: i, to: start)!)
        }
        return stride(from: 0, to: days.count, by: 7).map { Array(days[$0..<($0 + 7)]) }
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

extension Calendar {
    func startOfMonth(for date: Date) -> Date {
        self.date(from: self.dateComponents([.year, .month], from: date)) ?? date
    }
}
