import SwiftUI

struct RootView: View {
    @AppStorage("onboarding_done") private var onboardingDone: Bool = false
    @Environment(\.modelContext) private var modelContext
    @State private var pendingImport: SharedRoutine?

    var body: some View {
        Group {
            if !onboardingDone {
                OnboardingView()
            } else {
                mainTabs
            }
        }
        .onOpenURL { url in
            if let routine = parseRoutineLink(url.absoluteString) {
                pendingImport = routine
            }
        }
        .sheet(item: $pendingImport) { routine in
            ImportRoutineSheet(routine: routine) { keepIds in
                if !keepIds.isEmpty {
                    let imported = CustomRoutine(name: routine.name, stretchIds: keepIds)
                    modelContext.insert(imported)
                    try? modelContext.save()
                }
                pendingImport = nil
            }
            .presentationDetents([.medium, .large])
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

/// Makes SharedRoutine usable in `.sheet(item:)`.
extension SharedRoutine: Identifiable {
    var id: String { name + stretchIds.joined(separator: ",") }
}
