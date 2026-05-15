import SwiftUI

struct RootView: View {
    @AppStorage("onboarding_done") private var onboardingDone: Bool = false

    var body: some View {
        if !onboardingDone {
            OnboardingView()
        } else {
            mainTabs
        }
    }

    private var mainTabs: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label("Home", systemImage: "house.fill") }

            NavigationStack { ProgramsView() }
                .tabItem { Label("Programs", systemImage: "figure.flexibility") }

            NavigationStack { StretchesView() }
                .tabItem { Label("Stretches", systemImage: "figure.cooldown") }

            NavigationStack { CalendarView() }
                .tabItem { Label("Calendar", systemImage: "calendar") }

            NavigationStack { SettingsView() }
                .tabItem { Label("Settings", systemImage: "gear") }
        }
        .tint(.accentColor)
    }
}
