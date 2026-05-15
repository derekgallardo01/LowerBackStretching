# iOS

SwiftUI + SwiftData. Targets iOS 17 and up.

## First-time Xcode setup

The Swift sources are here in `LowerBackStretching/`, but the `.xcodeproj` is
not committed — Xcode prefers to generate that itself, and hand-edited pbxproj
files break easily. Steps:

1. Open Xcode 15+. **File → New → Project → iOS → App**.
2. Product Name: `LowerBackStretching`. Interface: **SwiftUI**. Language:
   **Swift**. Storage: **SwiftData**. Deployment target: **iOS 17.0**.
3. Save the project **inside this `ios/` directory**, replacing the existing
   `LowerBackStretching` folder when prompted (or save elsewhere and copy the
   files in).
4. In Finder, replace Xcode's generated files with the ones already here:
   - All Swift files under `LowerBackStretching/App/`, `Models/`, `Data/`,
     `Notifications/`, `Views/`.
   - `LowerBackStretching/Resources/stretches.json`, `programs.json`.
5. Back in Xcode, right-click the project → **Add Files to
   "LowerBackStretching"** → select all replaced files, ensure "Copy items if
   needed" is unchecked (they're already in place) and target membership is
   the app target.
6. In the project's `Info` settings, add `NSUserActivityTypes` if you plan to
   add Siri shortcuts (optional). Local notifications need no Info.plist entry.
7. Build & Run on a simulator (notifications also work in the simulator since
   iOS 16).

## Architecture

- `App/LowerBackStretchingApp.swift` — `@main` entry. Configures the SwiftData
  `ModelContainer` and requests notification permission on first launch.
- `Models/` — `Stretch`, `Program`, `ProgramDay` (Codable for JSON loading),
  plus `SessionRecord` (`@Model` for SwiftData).
- `Data/ContentLoader.swift` — loads bundled JSON.
- `Data/SessionStore.swift` — SwiftData queries: completed days, streak count.
- `Data/ReminderSettings.swift` — `@AppStorage` for the daily reminder
  on/off and time.
- `Notifications/ReminderManager.swift` — wraps
  `UNUserNotificationCenter` for daily repeating notifications.
- `Views/` — SwiftUI screens. `YouTubeView` is a `UIViewRepresentable`
  wrapping `WKWebView` for the iframe embed (no third-party SDK needed).

## Publishing

When you're ready for TestFlight / App Store:

1. Add an Apple Developer account in Xcode (Settings → Accounts).
2. Project → Signing & Capabilities → Team = your team.
3. Bundle ID: pick something unique like `com.yourname.lowerbackstretching`.
4. Product → Archive → Distribute App → App Store Connect.
