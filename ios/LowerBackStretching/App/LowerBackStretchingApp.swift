import SwiftUI
import SwiftData
import UserNotifications

@main
struct LowerBackStretchingApp: App {
    @StateObject private var content = ContentStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(content)
                .onAppear { requestNotificationPermission() }
        }
        .modelContainer(for: [SessionRecord.self])
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
    }
}
