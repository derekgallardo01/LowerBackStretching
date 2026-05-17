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

Three Gradle modules:

- **`:core`** — pure-JVM Kotlin library. Models (`Stretch`, `Program`,
  `ProgramDay`, `GlossaryEntry`, `EducationalCard`), the generic
  `PlayerEngine<T : Timed>` state machine, calendar math, gamification
  formulas (`computeStreak`, `longestStreak`, `xpProgress`, ...),
  achievements catalog, body-part / body-zone taxonomy, audio enums,
  settings enums (`ThemeMode`, `DurationUnit`), routine deep-link
  encoder/parser, cooldown-suggestion decision, flexibility delta math.
  No Android dependency — drives both `:app` and `:wear`.
- **`:app`** — the phone app. Depends on `:core`.
  - `App.kt` — Application class; constructs `AppDatabase`, repositories,
    `SyncController`, notification channel, audio singletons.
  - `MainActivity.kt` — single activity hosting Compose UI; intercepts
    `lowerbackstretching://routine?...` deep links for routine import.
  - `data/` — Room database + DAOs, repositories, JSON content loader,
    DataStore prefs.
  - `notifications/` — AlarmManager-based daily reminder + permission
    plumbing.
  - `audio/` — `AudioController` wrapping `MediaPlayer` + `SoundPool`
    for music / ambient / chime streams.
  - `health/`, `share/`, `sync/` — Health Connect, QR/deep-link share,
    cloud-sync interface (Firebase backend is opt-in).
  - `ui/` — Compose screens (`home`, `programs`, `stretches`, `player`,
    `calendar`, `settings`, `goals`, `achievements`, `flexibility`,
    `anatomy`, `learn`, `share`, `onboarding`, `routines`) + shared
    `components/`.
- **`:wear`** — Wear OS companion. Depends on `:core`. Reuses the same
  `PlayerEngine`; ships a slim `WatchStretch` and a minimal
  Compose-Wear UI. Standalone — no Data Layer API yet.

## Picture-in-Picture

The player activity supports PiP on Android 8 (API 26) and up. When
the user presses Home while on a player screen, the activity calls
`enterPictureInPictureMode(...)` with a 16:9 aspect ratio. The
Compose tree observes the change via `PictureInPictureHost.inPip`
and switches to a compact layout — just the YouTube video, the
remaining-seconds badge, and a 3dp progress bar at the bottom. No
controls (PiP windows are too small to hit them); tapping the PiP
expands the app back to full size.

The activity declares `supportsPictureInPicture="true"` and
`configChanges="screenSize|smallestScreenSize|screenLayout|orientation"`
in the manifest so Android doesn't recreate the activity on the
PiP transition.

## Wear OS companion

The `wear/` module is a standalone Wear OS app (Wear OS 3+ /
API 30+) that runs a stripped-down 5-stretch routine bundled in
its own `assets/watch_routine.json`. It doesn't talk to the phone
yet — every watch is self-contained — but pairs naturally with the
phone app because both share the same `applicationId`.

Build & install:

```sh
./gradlew :wear:assembleDebug
./gradlew :wear:installDebug   # to a connected Wear OS device or emulator
```

The watch app reuses the same launcher icon as the phone but ships
its own `WearMainActivity` (no shared activity / no Data Layer API
yet — that's a follow-up). The state machine is shared via
`:core` — both phone and watch use the generic
`PlayerEngine<T : Timed>`. `Stretch` and `WatchStretch` both
implement `Timed`; the watch's slim model stays slim, the engine
is identical.

## Health Connect

Wave 5 added an optional integration with Health Connect (Google's
cross-app health data store).

- Dependency: `androidx.health.connect:connect-client` (declared in
  `libs.versions.toml`). Pinned to `1.1.0-alpha07` — the newer
  `1.1.0-rc02` requires compileSdk 36 and AGP 8.9.1+, which would
  ripple through the whole project. Bump together when you're ready.
- Permissions: `health.WRITE_EXERCISE` (to log stretching sessions)
  and `health.READ_STEPS` (to suggest a cooldown after long walks).
  Both declared in `AndroidManifest.xml` and gated behind user
  toggles in Settings → Health Connect.
- The toggles do nothing unless the device has the Health Connect
  app installed. `HealthController.availability()` reports
  `NotInstalled` / `ProviderUpdateRequired` / `Available` and the
  Settings UI surfaces the right message.
- On Android 14+, Health Connect ships as a system module; on older
  devices users install it from the Play Store. The `<queries>` block
  in the manifest lets us discover the provider on Android 13 and
  below.
- Test on a real device — the emulator doesn't bundle Health Connect.

## Notifications

We use `AlarmManager.setRepeating` for the daily reminder. Android no longer
guarantees exact timing for inexact alarms; if you want stricter timing on
modern OS versions, swap to `WorkManager`'s periodic work request with a
scheduled `OneTimeWorkRequest` chain — the API in `ReminderScheduler` is
isolated so the call sites won't change.

## Tests

Two test source sets, both runnable from Studio (Run → Tests) or CLI.

### JVM unit tests (fast, no device)

```sh
./gradlew :core:test :app:testDebugUnitTest :wear:testDebugUnitTest
```

Tests in **`:core`** cover the pure logic — no Android, no Room:

| File | Covers |
|------|--------|
| `PlayerEngineTest` | tick, next/previous, pause, finish event, progress, routine-progress, startIndex |
| `DisplayTest` | difficulty capitalization, subtitles, formatDuration, body-part filtering, stretchCountSubtitle |
| `CalendarMonthTest` | grid math: leap year, boundary days, week starts |
| `ComputeStreakTest` | streak rule: today, grace-day, gaps |
| `GamificationTest` | longestStreak, XP curve, level lookup, weekly/monthly completions |
| `AchievementsTest` | unlock rules at various stat thresholds |
| `BodyPartsTest` | display formatting, distinctSorted, filterOptions |
| `BodyZoneTest` | tag → zone mapping |
| `CooldownSuggestionTest` | opt-in / stretched-today / threshold gates |
| `FlexibilityTest` | per-metric delta, missing-snapshot handling |
| `RoutineShareLinkTest` | build / parse round-trip, malformed links |
| `SyntheticProgramIdTest` | single/routine prefix classification |
| `SettingsTest` | ThemeMode + DurationUnit storage round-trips and fallbacks |
| `AudioTracksTest` | MusicTrack / AmbientTrack / ChimeTrack fromStorage |

Tests in **`:app`** cover the Room-bound + Android-bound code:

| File | Covers |
|------|--------|
| `CustomRoutineEntityTest` | CSV (de)serialization edge cases |
| `CustomRoutineRepositoryTest` | insert/update/duplicate/reorder/softDelete with fake DAO |
| `FlexibilityRepositoryTest` | record persists supplied measurements |
| `ProgramProgressRepositoryTest` | advance + cap + reset, synthetic-id no-op |
| `NoopSyncBackendTest` | every method returns the "not signed in" answer |
| `ReminderSchedulerTest` | next-occurrence math: same-day, next-day, rollovers |
| `FormatTest` | `formatTime(h, m)` zero-padding |

### Instrumented (Compose UI + Room DAO + full-app E2E)

Two ways to run these — pick whichever fits.

**Against a device you already have connected** (USB-debugged phone or a
running emulator):
```sh
./gradlew :app:connectedDebugAndroidTest
```

**Against managed virtual devices** (Gradle downloads the system image,
boots the AVD, runs the tests, and tears it down — no manual emulator
setup):
```sh
./gradlew :app:pixel6Api34DebugAndroidTest          # phone only
./gradlew :app:pixelTabletApi34DebugAndroidTest     # tablet only
./gradlew :app:phoneAndTabletGroupDebugAndroidTest  # both, in parallel
```

The managed-device profiles are declared in `app/build.gradle.kts` and
target API 34 with the AOSP ATD (automated test device) system image,
which boots faster than the standard emulator.

The "phone and tablet" group is the one to run before shipping; it
confirms the layout works on both form factors.

| File | Covers |
|------|--------|
| `AppDatabaseMigrationsTest` | Room migrations v2 → v6: column adds, table creates, defaults |
| `ContentRepositoryTest` | bundled JSON integrity, totalDurationSeconds |
| `SessionDaoTest` | Room insert/recent/completedDays/forDay |
| `CustomRoutineDaoTest` | Room insert/update/delete/byId |
| `SessionRepositoryIntegrationTest` | repository + real Room round-trip |
| `PrefsTest` | DataStore defaults + round-trip |
| `ReminderControllerTest` | `applyReminder` persists pref state |
| `CalendarIntentTest` | "Add to system calendar" intent construction |
| `HomeScreenTest` | header, streak card, program list |
| `ProgramsScreenTest` | header, category filter, FAB callback |
| `ProgramDetailScreenTest` | day rendering, onStartDay callback |
| `StretchesScreenTest` | body-part filter, navigate to detail |
| `StretchDetailScreenTest` | render + practice callback |
| `CalendarScreenTest` | header + empty state |
| `SettingsScreenTest` | reminder section + about |
| `RoutineBuilderE2ETest` | save disabled until name+selection, then enabled |
| `OnboardingE2ETest` | skip lands on home; step-through lands on home |
| `CompleteRoutineE2ETest` | full happy-path: pick program → finish day → see in calendar |

The Compose UI tests host screens inside `createAndroidComposeRule<ComponentActivity>()`
so `viewModel()` can construct AndroidViewModels with an Application context.
`CompleteRoutineE2ETest` uses `createAndroidComposeRule<MainActivity>()` instead
to exercise the real navigation graph; it wipes Room + DataStore in `@Before`.

## YouTube playback

`ui/components/YouTubePlayerView.kt` renders the YouTube iframe API inside a
WebView. No SDK key is required. If a video says "video unavailable, video
owner has disabled embedding", pick a different curated video — see
`content/README.md`.
