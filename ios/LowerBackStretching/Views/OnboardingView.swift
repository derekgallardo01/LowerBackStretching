import SwiftUI

struct OnboardingView: View {
    @AppStorage("onboarding_done") private var onboardingDone: Bool = false
    @State private var page: Int = 0

    private let pages = OnboardingPages.all
    private var isLastPage: Bool { page == pages.count - 1 }

    var body: some View {
        VStack(spacing: 24) {
            TabView(selection: $page) {
                ForEach(Array(pages.enumerated()), id: \.offset) { idx, p in
                    PageView(page: p).tag(idx)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .always))

            HStack {
                Button("Skip") { onboardingDone = true }
                    .foregroundStyle(.secondary)

                Spacer()

                Button(isLastPage ? "Turn on reminders" : "Next") {
                    if isLastPage {
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

private struct PageView: View {
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
