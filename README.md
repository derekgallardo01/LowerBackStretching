# Lower Back & Legs Stretching

Native Android (Kotlin / Jetpack Compose) and iOS (Swift / SwiftUI) stretching
app focused on lower back, legs, hips, and hamstrings. Features:

- Programs grouped by goal (lower back relief, sciatica, flexibility, etc.)
- Per-day routines with sequential stretch player
- YouTube video demonstrations for each stretch
- Calendar tracking with streaks and per-day completion history
- Local push notifications for daily reminders
- 100% local persistence — no accounts, no backend, no subscription

## Repo layout

```
content/        Shared JSON content (stretches, programs) consumed by both apps
android/        Android Studio / Gradle project (Kotlin + Compose)
ios/            iOS sources (SwiftUI) — create an Xcode project around them
```

The same `stretches.json` / `programs.json` files live in
`android/app/src/main/assets/` and `ios/LowerBackStretching/Resources/` so you
only edit content in one place if you keep them in sync (or symlink).

## Quick start

### Android

Open `android/` in Android Studio (Hedgehog or newer). Sync Gradle, plug in a
device or start an emulator, and Run.

Min SDK 26, Target SDK 34. Uses Compose, Room, WorkManager, and the official
`androidx.media3` plus a YouTube WebView embed for video playback.

### iOS

Open Xcode 15+, create a new iOS App project named `LowerBackStretching`,
SwiftUI lifecycle, iOS 17 deployment target. Drag the contents of
`ios/LowerBackStretching/` into the project (check "Copy items if needed" off
since they're already in place). Enable the Push Notifications capability is
not required for local notifications — only `UNUserNotificationCenter`
permission which the app requests at first launch.

See `ios/README.md` for the full step-by-step.

## Content / video IDs

Stretch entries use a `youtubeId` field. Placeholder IDs are shipped — replace
them with curated real video IDs before publishing. See `content/README.md`.

## What's done vs. what's next

Done in this scaffold:

- Project structure for both platforms
- Navigation between Home / Programs / Player / Calendar / Settings
- JSON content model + loaders on both platforms
- Local persistence (Room on Android, SwiftData on iOS) for completed sessions
- Streak calculation
- Local notification scheduling (WorkManager + AlarmManager on Android,
  UNUserNotificationCenter on iOS)
- YouTube player embed on both platforms

Next steps (left to you):

- Replace placeholder YouTube IDs with curated stretch videos
- Add more programs / stretches in the JSON files
- Polish UI (colors, typography, illustrations)
- App icons + launch screens
- Play Store / App Store listing assets
