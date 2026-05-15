# Android

Kotlin + Jetpack Compose. Targets Android 8.0 (API 26) and up.

## First-time setup

1. Install Android Studio (Hedgehog 2023.1.1 or newer).
2. Open this `android/` folder. Studio will prompt to install missing SDKs and
   sync Gradle.
3. Generate the Gradle wrapper jar (not checked in):
   ```sh
   gradle wrapper --gradle-version 8.7
   ```
   Or use Studio: File → Sync Project with Gradle Files. Studio will create
   `gradle/wrapper/gradle-wrapper.jar` on first build.
4. Plug in a device with USB debugging enabled (or start an emulator) and Run.

## Build from CLI

```sh
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug    # installs on a connected device
```

For release, set up a signing config in `~/.gradle/gradle.properties` and add
a `signingConfigs.release` block to `app/build.gradle.kts`, then:

```sh
./gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab  (upload to Play Console)
```

## Architecture

- `App.kt` — Application class; constructs `AppDatabase`, repositories, and
  the notification channel on startup.
- `MainActivity.kt` — Single activity hosting Compose UI. Requests
  `POST_NOTIFICATIONS` on first launch (Android 13+).
- `data/` — JSON content loader, Room database, repositories.
- `notifications/` — AlarmManager-based daily reminder. `BootReceiver`
  re-schedules after reboot.
- `ui/` — Compose screens (`home`, `programs`, `player`, `calendar`,
  `settings`) and the YouTube embed component.

## Notifications

We use `AlarmManager.setRepeating` for the daily reminder. Android no longer
guarantees exact timing for inexact alarms; if you want stricter timing on
modern OS versions, swap to `WorkManager`'s periodic work request with a
scheduled `OneTimeWorkRequest` chain — the API in `ReminderScheduler` is
isolated so the call sites won't change.

## YouTube playback

`ui/components/YouTubePlayerView.kt` renders the YouTube iframe API inside a
WebView. No SDK key is required. If a video says "video unavailable, video
owner has disabled embedding", pick a different curated video — see
`content/README.md`.
