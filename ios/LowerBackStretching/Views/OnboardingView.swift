import SwiftUI

struct OnboardingView: View {
    @AppStorage("onboarding_done") private var onboardingDone: Bool = false
    @State private var page: Int = 0

    private struct Page: Hashable {
        let title: String
        let body: String
        let systemImage: String
    }

    private let pages: [Page] = [
        Page(title: "Stretch with guided routines",
             body: "Pick a program by goal — lower back relief, hip openers, post-run cooldown. Each day plays a sequence of stretches with timers.",
             systemImage: "figure.flexibility"),
        Page(title: "Build your own",
             body: "Pick any stretches from the library to build a routine that fits you. Practice single stretches anytime.",
             systemImage: "figure.cooldown"),
        Page(title: "Stay consistent",
             body: "Track every session on the calendar. Streaks show your habit at a glance.",
             systemImage: "calendar"),
        Page(title: "Daily reminder (optional)",
             body: "A gentle nudge once a day so you don't forget. You can change the time or turn it off later.",
             systemImage: "bell.fill"),
    ]

    var body: some View {
        VStack(spacing: 24) {
            TabView(selection: $page) {
                ForEach(Array(pages.enumerated()), id: \.offset) { (idx, p) in
                    VStack(spacing: 20) {
                        Image(systemName: p.systemImage)
                            .font(.system(size: 56))
                            .frame(width: 96, height: 96)
                            .background(Circle().fill(Color.accentColor.opacity(0.18)))
                            .foregroundStyle(.tint)

                        Text(p.title).font(.title.weight(.semibold))
                            .multilineTextAlignment(.center)

                        Text(p.body).font(.body)
                            .multilineTextAlignment(.center)
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 12)
                    }
                    .padding(.horizontal, 24)
                    .tag(idx)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .always))

            HStack {
                Button("Skip") {
                    onboardingDone = true
                }
                .foregroundStyle(.secondary)

                Spacer()

                Button(page == pages.count - 1 ? "Turn on reminders" : "Next") {
                    if page == pages.count - 1 {
                        ReminderController.apply(
                            enabled: true,
                            hour: ReminderDefaults.hour,
                            minute: ReminderDefaults.minute,
                        )
                        onboardingDone = true
                    } else {
                        withAnimation { page += 1 }
                    }
                }
                .buttonStyle(.borderedProminent)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
        .padding(.top, 24)
    }
}
