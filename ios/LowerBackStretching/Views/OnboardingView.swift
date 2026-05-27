import SwiftUI

/// First-launch pager. The first page is the safety check
/// (`SafetyCheckPage`) — the remaining pages are the usual "what this
/// app does" intro from `OnboardingPages.all`. Tapping "One or more
/// applies" surfaces the full advisory in a sheet; both buttons there
/// dismiss back to the pager and advance.
struct OnboardingView: View {
    @AppStorage(SettingsKeys.onboardingDone) private var onboardingDone: Bool = false
    @AppStorage(SettingsKeys.redFlagScreeningCompletedAt) private var redFlagScreeningCompletedAt: Double = 0
    @AppStorage(SettingsKeys.streakNudgeEnabled) private var streakNudgeEnabled: Bool = true
    @State private var page: Int = 0
    @State private var showAdvisory: Bool = false

    private let pages = OnboardingPages.all
    // Page 0 is the safety check; subsequent pages are the regular intro.
    private var totalPages: Int { pages.count + 1 }
    private var isLastPage: Bool { page == totalPages - 1 }
    private var isSafetyPage: Bool { page == 0 }

    var body: some View {
        VStack(spacing: 24) {
            TabView(selection: $page) {
                SafetyCheckPage(onOneApplies: { showAdvisory = true })
                    .tag(0)
                ForEach(Array(pages.enumerated()), id: \.offset) { idx, p in
                    OnboardingPageView(page: p).tag(idx + 1)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .always))

            HStack {
                Button("Skip") { finishOnboarding(enableReminders: false) }
                    .foregroundStyle(.secondary)
                    .accessibilityIdentifier("onboardingSkip")

                Spacer()

                Button(primaryButtonTitle) {
                    if isSafetyPage { advanceFromSafety() }
                    else if isLastPage { finishOnboarding(enableReminders: true) }
                    else { withAnimation { page += 1 } }
                }
                .buttonStyle(.borderedProminent)
                .accessibilityIdentifier("onboardingPrimary")
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
        .padding(.top, 24)
        .sheet(isPresented: $showAdvisory) {
            NavigationStack {
                SafetyAdvisoryView(
                    onSeenDoctor: {
                        showAdvisory = false
                        advanceFromSafety()
                    },
                    onContinueAnyway: {
                        showAdvisory = false
                        advanceFromSafety()
                    }
                )
            }
        }
    }

    private var primaryButtonTitle: String {
        if isSafetyPage { return "None of these apply" }
        if isLastPage { return "Turn on reminders" }
        return "Next"
    }

    private func advanceFromSafety() {
        redFlagScreeningCompletedAt = Date.now.timeIntervalSince1970 * 1000
        withAnimation { page = 1 }
    }

    private func finishOnboarding(enableReminders: Bool) {
        if enableReminders {
            ReminderController.apply(
                enabled: true,
                hour: ReminderDefaults.hour,
                minute: ReminderDefaults.minute
            )
            StreakNudgeController.apply(enabled: streakNudgeEnabled)
        }
        onboardingDone = true
    }
}

private struct OnboardingPageView: View {
    let page: OnboardingPage

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: page.systemImage)
                .font(.system(size: 56))
                .frame(width: 96, height: 96)
                .background(Circle().fill(Color.accentColor.opacity(0.18)))
                .foregroundStyle(.tint)

            Text(page.title)
                .font(.title.weight(.semibold))
                .multilineTextAlignment(.center)

            Text(page.body)
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 12)
        }
        .padding(.horizontal, 24)
    }
}
