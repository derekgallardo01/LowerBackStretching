import SwiftUI

/// Entry point for the Apple Watch app. Standalone — doesn't require
/// the paired iPhone app to be running.
@main
struct StretchingWatchApp: App {
    var body: some Scene {
        WindowGroup {
            WatchPlayerView()
        }
    }
}
