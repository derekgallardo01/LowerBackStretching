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
                Text("Welcome back")
                    .font(.largeTitle.weight(.semibold))

                StreakCard(streak: streak, total: total)

                Text("Programs")
                    .font(.title2.weight(.semibold))
                    .padding(.top, 4)

                ForEach(content.programs) { program in
                    NavigationLink(value: program) {
                        ProgramCardView(program: program)
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
            Spacer()
            Stat(value: "\(streak)", label: "Day streak")
            Spacer()
            Stat(value: "\(total)", label: "Sessions")
            Spacer()
        }
        .padding(.vertical, 20)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.accentColor.opacity(0.15))
        )
    }
}

private struct Stat: View {
    let value: String
    let label: String
    var body: some View {
        VStack(spacing: 4) {
            Text(value).font(.largeTitle.weight(.semibold))
            Text(label).font(.caption.weight(.medium))
        }
    }
}

struct ProgramCardView: View {
    let program: Program
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(program.title).font(.title3.weight(.semibold))
            Text("\(program.days.count)-day · \(program.category.replacingOccurrences(of: "-", with: " "))")
                .font(.caption.weight(.medium))
                .foregroundStyle(.tint)
            Text(program.summary).font(.subheadline).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(.secondarySystemBackground)))
    }
}
