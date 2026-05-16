import Foundation

/// Date math for rendering a month grid. Returns weekday symbols and a
/// week-by-week list of dates that cover the month plus the leading/trailing
/// neighbour days needed to fill the first and last weeks. Pure — pulled
/// out of `CalendarView` so the view stays focused on layout.
struct CalendarMonth {
    let month: Date
    var calendar: Calendar = .current

    /// Weekday symbols rotated so the calendar's `firstWeekday` is first.
    var weekdaySymbols: [String] {
        var symbols = calendar.veryShortStandaloneWeekdaySymbols
        let offset = calendar.firstWeekday - 1
        return Array(symbols[offset...]) + Array(symbols[..<offset])
    }

    /// 5–6 rows of 7 days each. Cells outside `month` are still real Dates,
    /// useful for greying out the neighbour days in the UI.
    var weeks: [[Date]] {
        let firstOfMonth = calendar.startOfMonth(for: month)
        let range = calendar.range(of: .day, in: .month, for: firstOfMonth) ?? 1..<2
        let firstWeekdayOffset =
            (calendar.component(.weekday, from: firstOfMonth) - calendar.firstWeekday + 7) % 7
        let start = calendar.date(byAdding: .day, value: -firstWeekdayOffset, to: firstOfMonth)!
        let totalCells = ((firstWeekdayOffset + range.count + 6) / 7) * 7

        let days = (0..<totalCells).map {
            calendar.date(byAdding: .day, value: $0, to: start)!
        }
        return stride(from: 0, to: days.count, by: 7).map { Array(days[$0..<($0 + 7)]) }
    }

    func isInMonth(_ date: Date) -> Bool {
        calendar.isDate(date, equalTo: month, toGranularity: .month)
    }
}

extension Calendar {
    func startOfMonth(for date: Date) -> Date {
        self.date(from: self.dateComponents([.year, .month], from: date)) ?? date
    }
}
