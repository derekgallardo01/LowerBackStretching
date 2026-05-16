import Foundation

/// Static content for the first-launch flow. Pulled out of `OnboardingView`
/// so the view file is only responsible for layout.
struct OnboardingPage: Hashable {
    let title: String
    let body: String
    let systemImage: String
}

enum OnboardingPages {
    static let all: [OnboardingPage] = [
        .init(
            title: "Stretch with guided routines",
            body: "Pick a program by goal — lower back relief, hip openers, post-run cooldown. Each day plays a sequence of stretches with timers.",
            systemImage: "figure.flexibility"
        ),
        .init(
            title: "Build your own",
            body: "Pick any stretches from the library to build a routine that fits you. Practice single stretches anytime.",
            systemImage: "figure.cooldown"
        ),
        .init(
            title: "Stay consistent",
            body: "Track every session on the calendar. Streaks show your habit at a glance.",
            systemImage: "calendar"
        ),
        .init(
            title: "Daily reminder (optional)",
            body: "A gentle nudge once a day so you don't forget. You can change the time or turn it off later.",
            systemImage: "bell.fill"
        ),
    ]
}
