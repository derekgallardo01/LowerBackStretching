import SwiftUI
import SwiftData
import UserNotifications
import FirebaseCore

@main
struct LowerBackStretchingApp: App {
    @StateObject private var content = ContentStore()
    @AppStorage(SettingsKeys.themeMode) private var themeModeRaw: String = ThemeMode.system.storageValue

    init() {
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
        // Firebase session sync: configure only when the config file is
        // bundled, so builds without GoogleService-Info.plist keep the
        // no-op backend and make no network requests.
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
            SyncController.shared.backend = FirebaseSyncBackend()
        }
    }

    private let container: ModelContainer = {
        let config = ModelConfiguration(
            "main",
            isStoredInMemoryOnly: TestLaunchArgs.isResetData
        )
        return try! ModelContainer(
            for: SessionRecord.self, CustomRoutine.self, ProgramProgress.self,
            FlexibilityTest.self, PainLog.self,
            configurations: config
        )
    }()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(content)
                .preferredColorScheme(ThemeMode.fromStorage(themeModeRaw).colorScheme)
        }
        .modelContainer(container)
    }
}

/// Launch arguments understood only when running under XCUITest.
enum TestLaunchArgs {
    /// `-resetData` makes the SwiftData store ephemeral (in-memory) so
    /// each test starts from a clean slate.
    static var isResetData: Bool {
        CommandLine.arguments.contains("-resetData")
    }
}
