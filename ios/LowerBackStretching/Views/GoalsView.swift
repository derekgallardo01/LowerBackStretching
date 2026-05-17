import SwiftUI
import SwiftData

struct GoalsView: View {
    @Query private var sessions: [SessionRecord]
    @AppStorage(SettingsKeys.weeklyGoal) private var weeklyGoal: Int = GoalDefaults.weekly
    @AppStorage(SettingsKeys.monthlyGoal) private var monthlyGoal: Int = GoalDefaults.monthly

    private var completedDays: Set<Date> { SessionStore.completedDays(from: sessions) }
    private var weekly: Int { weeklyCompletions(days: completedDays) }
    private var monthly: Int { monthlyCompletions(days: completedDays) }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                GoalCard(
                    title: "This week",
                    completed: weekly,
                    target: $weeklyGoal,
                    range: 1...14
                )
                GoalCard(
                    title: "This month",
                    completed: monthly,
                    target: $monthlyGoal,
                    range: 1...30
                )
            }
            .padding(16)
        }
        .navigationTitle("Goals")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct GoalCard: View {
    let title: String
    let completed: Int
    @Binding var target: Int
    let range: ClosedRange<Int>

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.headline)
            Text("\(completed) of \(target) days")
                .font(.title3)
                .foregroundStyle(Color.accentColor)
            ProgressView(
                value: target == 0 ? 0 : min(Double(completed) / Double(target), 1)
            )
            Text("Target: \(target)")
                .font(.caption)
                .foregroundStyle(.secondary)
            Stepper(value: $target, in: range) { EmptyView() }
                .labelsHidden()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
}
