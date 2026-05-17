# Apple Watch companion

Swift sources for a standalone watchOS app — runs a 5-stretch
lower-back routine off the wearer's wrist, no paired iPhone
required. Lives at `ios/WatchApp/` rather than under
`LowerBackStretching/` so it can be added as its own target in
Xcode without colliding with the iPhone sources.

## Adding the target to Xcode

These steps mirror `ios/README.md` for the iPhone target:

1. Open the `LowerBackStretching.xcodeproj` you generated for the
   iPhone app.
2. **File → New → Target → watchOS → App**. Product Name:
   `StretchingWatchApp`. Interface: **SwiftUI**. Language:
   **Swift**. Deployment target: **watchOS 10.0** (matches the
   SwiftUI features used: `.onChange(of:initial:)`, `.task`,
   `.monospacedDigit`).
3. Save the target inside this repo's `ios/` directory — name the
   folder `WatchApp` and overwrite the Xcode-generated files with
   the ones already here (`App/`, `Models/`, `Player/`, `UI/`,
   `WatchContent.swift`, `WatchHaptics.swift`, plus
   `Resources/watch_routine.json`).
4. In Xcode, right-click the **StretchingWatchApp** target → **Add
   Files to "StretchingWatchApp"** → select every file under
   `ios/WatchApp/`. Make sure target membership is the watch app
   target, *not* the iPhone target.
5. Build & Run on a paired Apple Watch simulator (watchOS 10+).

## Architecture

| File | What it does |
| --- | --- |
| `App/WatchApp.swift` | `@main` entry. WindowGroup hosting `WatchPlayerView`. |
| `Models/WatchModels.swift` | Slim `WatchStretch` + `WatchRoutine` Codables. |
| `Player/WatchPlayerEngine.swift` | Pure state machine. Mirrors the iPhone `PlayerEngine` line-for-line. |
| `WatchContent.swift` | Loads `watch_routine.json` from the bundle. |
| `WatchHaptics.swift` | Wraps `WKInterfaceDevice.play(.click / .success)`. |
| `UI/WatchPlayerView.swift` | SwiftUI screen: title, big remaining-seconds, progress bar, 3 controls. |
| `Resources/watch_routine.json` | Bundled 5×30s routine. |

## What it doesn't do yet

- No iPhone↔Watch WatchConnectivity sync. The watch is fully
  standalone; routines created on the phone don't appear here.
  Wiring `WCSession` is a follow-up.
- The engine is duplicated rather than pulled into a shared Swift
  Package. When the drift becomes uncomfortable, that's the
  natural extraction (one package depended on by both targets).

## Tests

Add a `StretchingWatchAppTests` target in Xcode if you want to test
the watch engine directly. The state-machine semantics are already
locked down by:

- `ios/LowerBackStretchingTests/PlayerEngineTests.swift` (iPhone-side
  `PlayerEngine` — `WatchPlayerEngine` mirrors it line-for-line)
- `android/wear/src/test/.../WearPlayerEngineTest.kt` (Wear OS-side,
  same algorithm)

So a third test bundle is optional; if you add it, copy the 11 cases
from the Wear OS test file and translate them to Swift.
