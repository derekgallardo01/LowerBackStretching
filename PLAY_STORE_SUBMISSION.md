# Google Play Submission Guide — Lower Back Stretching

End-to-end instructions to take this app from "compiled debug APK on an emulator" to "publicly listed on Google Play." Every form, every form field, every answer.

---

## 0. Decisions you need to make first

These can't be inferred from the code. Lock them in before opening Play Console.

| Decision | Recommendation | Notes |
|---|---|---|
| App name (Play listing) | **Lower Back Stretching** | Matches `strings.xml`. Stable. |
| Package / Application ID | `com.lowerbackstretching` (already set) | Permanent — cannot change after first publish. |
| Pricing | **Free** | Easiest path for a v1 wellness app. Paid + IAP can come later. |
| Target audience age range | **18+** | Health/exercise app with self-reported pain data. Below 18 invites COPPA + family-program complexity. |
| Category | **Health & Fitness** | Primary. |
| Tags (up to 5) | **`Stretching`**, **`Yoga`**, **`Posture`**, **`Mobility`**, **`Mindfulness`** | Picked from Play Console's dropdown. Alternates if any aren't available: `Wellness`, `Pain relief`, `Pilates`, `Recovery`. Avoid: `Weight loss`, `Cardio`, `HIIT`, `Meditation` (wrong frame). |
| Distribution countries | **All countries** (default) | Restrict later if needed. |
| Privacy policy URL | **You must host one.** | See §6 — template is in [PRIVACY.md](PRIVACY.md). Easiest: a GitHub Pages site or a free Carrd/Notion page. |
| Developer account name | Your decision | Will appear in the listing. |
| Support email | Your decision | Required field. |

---

## 1. Pre-flight checklist

- [ ] Google Play Developer Account created and verified ($25 one-time fee, ID verification can take 1–3 days)
- [ ] Decide on the items in §0
- [x] Privacy policy live at <https://derekgallardo01.github.io/LowerBackStretching/PRIVACY> (§6)
- [x] `versionName = "1.0.0"` and signing scaffold wired in [android/app/build.gradle.kts](android/app/build.gradle.kts) (§3)
- [x] 512×512 Play Store icon generated at [play-store-icon-512.png](play-store-icon-512.png) (§2)
- [x] 1024×500 feature graphic generated at [feature-graphic-1024x500.png](feature-graphic-1024x500.png) (§7)
- [x] 8 phone + 8 tablet screenshots captured at [screenshots/phone/](screenshots/phone/) and [screenshots/tablet/](screenshots/tablet/) via the new `GeneratePlayScreenshotsTest` (§7)
- [ ] Generate an upload keystore (see §3b)
- [ ] Build a signed release AAB (see §4)

---

## 2. App icon

The app ships an adaptive icon at [android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml](android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml):

- Background: `#5C7A65` (Sage)
- Foreground: a stylized cream ring on the sage field
- Themed (Android 13+) variant: same path stripped of color

**Play's 512×512 hi-res PNG is already generated and committed at** [play-store-icon-512.png](play-store-icon-512.png). Upload it to Play Console under *Main store listing → App icon*.

To regenerate (e.g. after a color or shape tweak):

```bash
python scripts/generate_play_store_icon.py
# Writes play-store-icon-512.png at the repo root.
```

The script ([scripts/generate_play_store_icon.py](scripts/generate_play_store_icon.py)) replays the adaptive icon's outer + inner ellipses onto a 512×512 PNG canvas with the sage background. Needs Python 3.8+ and Pillow (`pip install Pillow`) — no Android Studio required.

**Honest assessment:** the icon is functional but minimalist (a simple ring shape). It won't stand out next to Calm/Headspace on a search results page. **Recommended path:**

- **Closed / open testing tracks:** ship this icon as-is. It's adaptive, has the Android 13+ monochrome variant, and meets every technical Play requirement.
- **Production launch:** commission a designer (~$50–$200 on Fiverr/99designs) for a more distinctive mark. Some directions worth briefing on: a stylized spine "S" with a lower-back accent, a Calm-style leaf shape, or a stretching-figure silhouette. Once the new vector lands at [android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_foreground.xml](android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_foreground.xml), re-run the generator script to get a matching 512×512 PNG.

---

## 3. Version + signing config — `android/app/build.gradle.kts`

### 3a. Bump version (one-line change before each release)

```kotlin
defaultConfig {
    ...
    versionCode = 1          // increment by 1 every release
    versionName = "1.0.0"    // semver string shown to users
    ...
}
```

Set `versionCode = 1, versionName = "1.0.0"` for your first publish.

### 3b. Release signing — generate keystore, then wire it

**Generate your upload keystore** (do this ONCE, store the file securely — losing it = you can never publish updates to this app):

```bash
keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
# Answer the prompts. Use a STRONG password. Note it down.
```

Move the keystore OUTSIDE the repo (e.g., `~/.android/keystores/lowerback-upload.jks`) and create `keystore.properties` in the project root (already gitignored if you keep `.properties` ignored — check `.gitignore`):

```properties
storeFile=/absolute/path/to/lowerback-upload.jks
storePassword=YOUR_STRONG_PASSWORD
keyAlias=upload
keyPassword=YOUR_STRONG_PASSWORD
```

Add the signing block to `android/app/build.gradle.kts`:

```kotlin
import java.util.Properties
import java.io.FileInputStream

android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            val keystoreProperties = Properties()
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false        // turn on once you've audited Proguard
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**Important:** add `keystore.properties` and `*.jks` to `.gitignore` if they're not already there — never commit either.

---

## 4. Build the release App Bundle (AAB)

Play requires AAB (not APK) for new submissions.

```bash
cd android
./gradlew :app:bundleRelease
```

Output: `android/app/build/outputs/bundle/release/app-release.aab`

Verify it's signed:

```bash
"$ANDROID_HOME/build-tools/34.0.0/apksigner" verify --print-certs --verbose app/build/outputs/bundle/release/app-release.aab
```

(Or use Android Studio: **Build → Generate Signed Bundle / APK → Android App Bundle**, point at your keystore.)

---

## 5. Store listing copy — paste these into Play Console

### Title (max 30 chars)
```
Lower Back Stretching
```
*(21 chars — fits.)*

### Short description (max 80 chars)
```
Short guided routines to ease lower back pain and build a daily stretching habit.
```
*(80 chars exactly — paste verbatim.)*

### Full description (max 4000 chars — paste verbatim)
```
Short, guided stretching routines built around your lower back. No equipment, no gym, no time excuses — five minutes between meetings is enough to start feeling better.

WHAT'S INSIDE

• Guided programs by goal — lower-back relief, hip openers, post-run cooldown
• Build your own routine from 25+ named stretches
• A clear timer for every stretch with one-handed controls
• Streak tracking and gentle daily reminders
• Pain check-ins before and after each session so you can see what's actually helping
• Flexibility self-tests to track your reach over time

DESIGNED FOR YOUR BACK

Every routine is built around the muscles that actually drive lower back pain — the lumbar spine, hip flexors, glutes, and hamstrings. We pair a body diagram with each stretch so you understand what you're working on.

SAFE BY DEFAULT

Some back pain needs a doctor, not stretching. We screen for red-flag symptoms (radiating leg pain, numbness, post-trauma severe pain, fever with back pain) during onboarding and route you to a clinician when those apply. You can review the safety check any time from Settings.

HABIT, NOT OBLIGATION

Stretch on the days you can. The app's streak safety net keeps you motivated without punishing missed days. Milestones at 7, 30, 100, and 365 days celebrate the work you've actually done.

PRIVATE BY DESIGN

All your data — pain logs, flexibility tests, completed sessions — stays on your device unless you opt into Health Connect for steps and exercise logging. No accounts, no ads, no tracking.

GOOD FOR

• Office workers with a stiff lower back from sitting
• Anyone recovering from a long flight or drive
• Runners and lifters who skip mobility work
• Anyone who's been told to "stretch more" but doesn't know where to start

This is not a replacement for medical care. If you have severe, persistent, or unusual back symptoms, please see a clinician.
```

### "What's new" (release notes, max 500 chars)
```
First release. 25+ guided stretches, programs for lower-back relief, daily reminders, pain check-ins, flexibility tests, streak tracking with a forgiving safety net, and a built-in safety screen for symptoms that warrant a doctor's visit before exercise.
```

### Categorization
- **Category:** Health & Fitness
- **Tags:** Stretching, Yoga, Posture, Mobility, Mindfulness (see §0 for alternates)
- **App access:** All functionality available without login
- **Ads:** No
- **Content rating:** see §8

---

## 6. Privacy policy — `PRIVACY.md`

A privacy policy URL is **required** for any app on Play. Template is in [PRIVACY.md](PRIVACY.md).

### Hosting options (free, ranked by least friction)

1. **GitHub Pages** (recommended) — push `PRIVACY.md` to a repo, enable Pages on `main` → URL becomes `https://<user>.github.io/<repo>/PRIVACY.html`. Convert MD to HTML or rename to `index.html` + commit.
2. **Carrd** (carrd.co) — free single-page site, paste the markdown. ~10 minutes.
3. **Notion public page** — paste, share as public, copy the URL.

You'll paste the final URL into Play Console at:

> *Policy → App content → Privacy Policy → URL field.*

Customise the template for your specific email and effective date before hosting.

---

## 7. Screenshots + feature graphic

### Required: at least 2 phone screenshots, max 8

**Use the Pixel_Phone AVD** (or your real device) running a populated state:

1. Onboard, complete a couple of sessions across different programs, log some pain, finish a routine on day 7 to capture the milestone modal.
2. Capture via the emulator's screenshot button or:
   ```bash
   adb shell screencap -p /sdcard/screen.png
   adb pull /sdcard/screen.png ./screenshots/01-home.png
   ```

**Recommended 8 phone screenshots** (1080×2400 portrait):

1. **Home** — streak card, programs list — first impression
2. **Programs detail** — Lower Back Relief 7-day program with day list
3. **Player** — mid-stretch, big timer visible, body silhouette
4. **Pain check-in dialog** — slider + body diagram
5. **FinishedView** — with confetti, streak +1, "Unlocked: First steps"
6. **Pain history** — populated with a session delta showing improvement
7. **Calendar** — month with several days marked
8. **Safety check** — onboarding page, builds trust

### Required: feature graphic (1024×500 PNG)

A wide banner that appears at the top of the Play listing. Quickest path: open Figma/Canva, drop the sage `#5C7A65` background, place the app name in white serif on the left, mockup phones with screenshots on the right. **~30 min in Canva** — they have a "Play Store Feature Graphic" template.

### Optional: 7" tablet screenshots, 10" tablet screenshots, Wear OS screenshots

Not required to launch. Add later if you want the tablet listing to look better than auto-scaled phone shots.

---

## 8. Content rating questionnaire — exact answers

When Play Console asks you to fill the IARC questionnaire, here are the answers for this app:

| Question | Answer |
|---|---|
| Does your app contain violence? | No |
| Does your app contain sexual content? | No |
| Does your app contain profanity? | No |
| Does your app contain drug/alcohol references? | No |
| Does your app contain gambling? | No |
| Does your app contain user-generated content? | No (custom routines are local-only and not shared by default) |
| Does your app share user location? | No |
| Does your app allow user-to-user communication? | No |
| Does your app collect personal info? | Yes — see §9 (Data Safety form); the IARC questionnaire is separate and answered honestly: pain ratings and exercise data are stored locally |
| Does your app simulate gambling? | No |
| Does your app contain controversial content? | No |
| Does your app have an age-restricted account system? | No |

Expected outcome: **Everyone** or **Everyone 10+** rating in all regions.

---

## 9. Data Safety form — exact answers

Play Console → App content → Data Safety. Required for every app.

### Does your app collect or share user data?
**Yes** — for the pain logs, session history, flexibility tests stored on device, AND optional Health Connect read/write.

### Data types collected

| Type | Collected? | Optional? | Purpose | Shared? |
|---|---|---|---|---|
| Health and fitness — *Fitness info* | Yes | Required (the app's core feature) | App functionality, Analytics (internal only) | No |
| Health and fitness — *Health info* (pain logs, body location) | Yes | Optional (user can skip every prompt) | App functionality | No |
| App activity — *Other in-app actions* (which programs, completed days) | Yes | Required | App functionality | No |
| Personal info — *Name, email, address, phone, IDs* | No | — | — | — |
| Financial info | No | — | — | — |
| Location | No | — | — | — |
| Web browsing | No | — | — | — |
| App info — *Crash logs, diagnostics* | No (you haven't wired Crashlytics) | — | — | — |
| Photos / videos / audio files | No | — | — | — |
| Files and docs | No | — | — | — |
| Calendar events | No (the "Schedule a break" intent hands off to Google Calendar — that's a calendar handoff, not collection) | — | — | — |

### Security practices

| Question | Answer |
|---|---|
| Is data encrypted in transit? | Yes — when Health Connect is used, all transport is Android-system-managed. When stored on-device, encryption is per the user's device-level encryption. |
| Can users request data be deleted? | Yes — uninstalling the app deletes all on-device data. There is no server. |
| Do you follow Google Play Families Policy? | Not applicable — app is 18+ |
| Has your data collection practices been independently validated? | No |

### Health Connect integration

If you use Health Connect read (steps) or write (exercise sessions) — declare this in the Health Connect-specific data flow. The app currently does both **opt-in only** behind Settings toggles. Make sure to declare this honestly.

---

## 10. App content declarations — every checkbox

In Play Console under "App content," fill all of these:

| Section | Answer |
|---|---|
| **Privacy policy URL** | Your hosted URL from §6 |
| **App access** | All functionality available without restrictions (no login required) |
| **Ads** | This app does not contain ads |
| **Content rating** | Complete the IARC questionnaire (see §8) |
| **Target audience** | 18+ |
| **News app** | No |
| **COVID-19 contact tracing** | No |
| **Data safety** | Complete the form (see §9) |
| **Government apps** | No |
| **Financial features** | No |
| **Health features** | Yes (Health Connect read/write, optional) |
| **Tax category** | Service (Health & Fitness) |

---

## 11. Release tracks — which to use

Recommended progression:

1. **Internal testing** (up to 100 testers, instant rollout) — *do this first.* Upload the AAB, add your own email and 2–3 friends, install via the testing link.
2. **Closed testing** (up to thousands of testers, 1–3 day review) — once internal is stable, open up to ~50 people for a week. Helps catch device-specific bugs you couldn't repro.
3. **Open testing** (anyone with the link) — optional intermediate step. Useful for collecting Play Store reviews before main launch.
4. **Production** — full public listing. First-time submissions get a 1–3 day human review.

To submit:

> Play Console → Production (or Testing → Internal first) → Create new release → Upload AAB → Add release notes → Save → Review release → Submit for review.

---

## 12. Post-submission — what to expect

- **Review time:** First submission is typically reviewed in 1–7 days. Subsequent updates are usually 24h or less.
- **Common rejection reasons for health apps:**
  - Privacy policy URL not reachable / missing
  - Health Connect permissions declared in manifest but no UI to enable
  - Claims in the listing copy that imply medical treatment ("cure" / "diagnose")
  - **Our copy avoids all of these** — it positions the app as a wellness/habit tool, not a medical device.

- After approval, the listing goes live within a few hours.

---

## 13. Critical "don't ship without this" checklist

Final gate before clicking "Submit for review":

- [ ] `versionCode` incremented since last release (always +1, never decrease)
- [ ] AAB is signed with the upload keystore (`apksigner verify` passes)
- [ ] Privacy policy URL is live and reachable
- [ ] Screenshots have no personal info accidentally captured (your real name, etc.)
- [ ] App opens cleanly on a fresh install (no crash, no missing assets)
- [ ] At least 14 of 26 stretches have working video IDs (already done — the other 12 fall back to the no-video panel)
- [ ] `./gradlew :app:testDebugUnitTest :core:test` passes locally
- [ ] `./gradlew :app:connectedDebugAndroidTest` passes on a Pixel emulator
- [ ] The `app_name`, launcher icon, splash bg are what you expect on the home screen of a clean install

---

## 14. Useful commands cheat-sheet

```bash
# From repo root:

# Build a signed release AAB
cd android && ./gradlew :app:bundleRelease

# Build a release APK for sideload-testing (no signing needed for debug)
./gradlew :app:assembleRelease

# Run the full test suite
./gradlew :core:test :app:testDebugUnitTest

# Run instrumented tests against attached devices/emulators
./gradlew :app:connectedDebugAndroidTest

# Verify the AAB signature
"$ANDROID_HOME/build-tools/34.0.0/apksigner" verify --print-certs \
  app/build/outputs/bundle/release/app-release.aab

# Capture a screenshot from a connected device
adb shell screencap -p /sdcard/s.png && adb pull /sdcard/s.png ./

# Install a release APK locally (for spot-checking before bundling)
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## Open questions to confirm before you click submit

1. Are you using your own name as the developer, or a business/LLC?
2. Do you have a domain or social handle to use as the support URL?
3. Are you OK with the existing minimalist icon, or do you want pro art first?
4. Free, free with future IAP, or paid?
5. Worldwide or restricted countries?

Once those are answered, every other field in the Console can be filled directly from this document.
