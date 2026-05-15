import SwiftUI

struct RootView: View {
    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label("Home", systemImage: "house.fill") }

            NavigationStack { ProgramsView() }
                .tabItem { Label("Programs", systemImage: "figure.flexibility") }

            NavigationStack { CalendarView() }
                .tabItem { Label("Calendar", systemImage: "calendar") }

            NavigationStack { SettingsView() }
                .tabItem { Label("Settings", systemImage: "gear") }
        }
        .tint(.accentColor)
    }
}
