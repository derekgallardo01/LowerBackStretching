import SwiftUI
import SwiftData

@main
struct LowerBackStretchingApp: App {
    @StateObject private var content = ContentStore()
    @AppStorage(SettingsKeys.themeMode) private var themeModeRaw: String = ThemeMode.system.storageValue

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
