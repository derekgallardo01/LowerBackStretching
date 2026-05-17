# Lower Back & Legs Stretching

Native Android (Kotlin / Jetpack Compose) + Wear OS companion + iOS (Swift /
SwiftUI) stretching app focused on lower back, legs, hips, and hamstrings.

## What it does

- **9 built-in programs** grouped by goal — lower-back relief, sciatica,
  hip openers, post-run cooldown, before-bed wind-down, and more.
- **27-stretch library** with body-part filters and YouTube video
  demonstrations.
- **Custom routines** — pick any stretches, save, reorder, duplicate, share
  via deep link or QR code.
- **Player** with timer, pause/skip, picture-in-picture, screen-on,
  orientation lock, configurable haptics, and optional music / ambient /
  chime audio.
- **Tracking** — calendar, streaks, weekly/monthly goals, achievements,
  XP & levels, longest streak.
- **Health Connect** integration (opt-in) — write stretching sessions,
  pull step counts to suggest a cooldown after long walks.
- **Education** — glossary, "why this stretch" cards, "what you should
  feel" overlays during the player.
- **Body diagram** — tap a silhouette to see which stretches target that
  zone. Anatomy overlay during the player.
- **Cloud sync** (opt-in, optional Firebase backend) and **first-launch
  onboarding** that sets up the daily reminder.
- **Watch companion** — Wear OS app with timer + haptic cues, no phone
  needed during the routine.

100% local persistence by default; cloud sync only kicks in if you sign in.

## Repo layout

```
content/        Shared JSON (stretches, programs, glossary) consumed by both apps
android/        Gradle project — three modules: :core (pure JVM), :app, :wear
ios/            iOS sources — LowerBackStretching/ (phone) + WatchApp/ (watch)
firebase/       Firestore rules + cloud-functions stubs (backend, optional)
.github/        Actions workflows (unit tests on every push, emulator nightly)
```

The same `stretches.json` / `programs.json` / `glossary.json` files live in
`android/app/src/main/assets/` and `ios/LowerBackStretching/Resources/` —
keep them in sync (or symlink) when editing content.

### Android modules

- `:core` — pure-JVM Kotlin library. Models, the generic `PlayerEngine<T :
  Timed>` state machine, calendar math, gamification calculations, audio
  enums, formatting helpers. No Android dependency, no Room, no DataStore.
- `:app` — the phone Android app. Depends on `:core`. Owns Room
  persistence, DataStore prefs, Compose screens, Health Connect /
  notifications / WorkManager / cloud-sync plumbing.
- `:wear` — Wear OS companion. Depends on `:core`. Reuses the same
  PlayerEngine; ships a slim `WatchStretch` model.

## Quick start

### Android

Open `android/` in Android Studio (Hedgehog or newer). Sync Gradle, plug in
a device or start an emulator, and Run. Min SDK 26, Target SDK 34.

```sh
./gradlew :app:installDebug              # phone
./gradlew :wear:installDebug             # watch
./gradlew :core:test :app:testDebugUnitTest :wear:testDebugUnitTest
```

### iOS

Open Xcode 15+, create a new iOS App named `LowerBackStretching`, drag in
`ios/LowerBackStretching/`. Add a Watch App target named `WatchApp`, drag
in `ios/WatchApp/`. iOS 17 deployment target. See `ios/README.md` for
the full step-by-step.

## Content / video IDs

Stretch entries use a `youtubeId` field. Placeholder IDs ship in the repo —
replace them with curated real video IDs before publishing. See
`content/README.md`.

## CI

- `.github/workflows/android-tests.yml` — JVM unit tests on every push to
  `main` and on PRs that touch `android/**`. Per-module diagnostic captures
  Kotlin compile errors into the run summary on failure.
- `.github/workflows/android-instrumented-tests.yml` — emulator tests
  (Pixel 6 + Pixel Tablet) on a nightly schedule and manual dispatch.
  Includes Room migration smoke tests.

## What's done

The brainstorm plan shipped Waves 1–10:

| # | Wave | Status |
|---|---|---|
| 1 | Foundation polish (dark mode, units, haptics, lifecycle save-state) | ✓ |
| 2 | Audio (music + ambient + chime + ducking) | ✓ |
| 3 | Routine management (reorder, duplicate, soft-delete, share-via-link) | ✓ |
| 4 | Gamification (streaks, goals, XP, levels, achievements) | ✓ |
| 5 | Health integration (Health Connect write + read steps) | ✓ |
| 6 | Education (glossary, "why this stretch", what-you-should-feel) | ✓ |
| 7 | Anatomy & body diagram (tap silhouette → routine) | ✓ |
| 8 | Backend & social (sync interface; Firebase impl is opt-in) | ✓ |
| 9 | Watch companion (Wear OS app sharing PlayerEngine) | ✓ |
| 10 | Polish (PiP, share QR, system-calendar integration) | ✓ |

## What's next

- Replace placeholder YouTube IDs with curated stretch videos
- Bundle the audio MP3s (see `android/app/AUDIO_FILES.md`) — currently
  the audio system silently no-ops when files are missing
- Wire a real Firebase project (the `FirebaseSyncBackend` stub already
  exists; only the `google-services.json` and a few config swaps remain)
- App icons, launch screens, store listing assets
