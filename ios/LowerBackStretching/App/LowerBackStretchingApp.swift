import SwiftUI
import SwiftData
import UserNotifications

@main
struct LowerBackStretchingApp: App {
    @StateObject private var content = ContentStore()

    private let container: ModelContainer = {
        let config = ModelConfiguration(
            "main",
            isStoredInMemoryOnly: TestLaunchArgs.isResetData
        )
        return try! ModelContainer(
            for: SessionRecord.self, CustomRoutine.self,
            configurations: config
        )
    }()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(content)
                .onAppear { requestNotificationPermission() }
        }
        .modelContainer(container)
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
    }
}

/// Launch arguments understood only when running under XCUITest.
enum TestLaunchArgs {
    /// `-resetData` makes the SwiftData store ephemeral (in-memory) so each
    /// test starts from a clean slate.
    static var isResetData: Bool {
        CommandLine.arguments.contains("-resetData")
    }
}
