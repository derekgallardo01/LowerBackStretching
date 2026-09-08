# Cloud Sync Launch Checklist (v1.1.0)

Anonymous session sync is fully implemented on both platforms (`FirebaseSyncBackend`
+ push hook on session completion, sync ON by default with the Settings toggle to
disable). The app builds are blocked on the Firebase config files; the cloud side
is blocked on four one-time steps.

## 1. Cloud setup (one-time)

Firebase was already added to the existing `lower-back-stretching` GCP project
(done 2026-09-08 via the service account). Remaining, in the
[Firebase console](https://console.firebase.google.com/project/lower-back-stretching):

1. **Add apps**: Android app `com.lowerbackstretching` and iOS app
   `com.derekgallardo.lowerbackstretching` → download `google-services.json`
   (→ `android/app/`) and `GoogleService-Info.plist` (→ `ios/LowerBackStretching/`).
   Both are gitignored — keep them local + CI secrets.
2. **Firestore**: Create database (production mode, `nam5` or your preferred region).
3. **Authentication**: Sign-in method → enable **Anonymous**.
4. **Rules + indexes**: from `firebase/`: `npm i -g firebase-tools && firebase login`,
   `firebase use --add` (alias `default`), then
   `firebase deploy --only firestore:rules,firestore:indexes`.

> Claude can do steps 1–3 via the management API using the existing service
> account — the permission classifier blocked those calls this session; approve
> the google-API Bash commands next session and they take ~2 minutes.

## 2. CI

`android-release.yml` (and `android-tests.yml` if it configures `:app`) needs
`google-services.json` restored from a new repo secret (e.g.
`GOOGLE_SERVICES_JSON_BASE64`) before gradle runs — same pattern as the keystore.
**Do not push this branch until that secret + file step is in place, or CI goes red.**

## 3. Store compliance (required — sync is ON by default)

- **Play Data Safety**: declare collection of "App activity → App interactions"
  (session completions: program id, day, duration, timestamp), tied to an
  anonymous ID; not shared; user can request deletion (toggle off = stop).
- **App Store privacy**: add "Product Interaction" under Data Not Linked to You.
- **Privacy policy**: add a section describing anonymous session sync and the
  Settings toggle. What is NOT collected: pain logs (deliberately excluded from
  sync and from the Firestore rules), calendar/health data, identity.

## 4. Ship

- Android: versionCode **16** / versionName **1.1.0** (set). CI release workflow
  or local `:app:bundleRelease`; upload keystore unchanged.
- iOS: MARKETING_VERSION **1.1.0** / build **6** (set). `scripts/release-ios.sh`
  (xcodegen will resolve the new Firebase SPM package on first run).
- Wear (v0.1.2) unchanged — watch sessions sync via the phone in a follow-up.

## 5. After launch

kinetichelix.io already has the adapter live: it counts
`collectionGroup("sessions")` (total + 7-day) with the same service account and
shows them only once nonzero — the site starts ticking the moment the first
synced session lands. Daily Play install stats additionally need
`LOWERBACK_PLAY_BUCKET` on the site's Railway env (Play Console → Download
reports → Copy Cloud Storage URI).

## Security note (do before or with this release)

`lower-back-stretching-510d86790997.json` (repo root) is a live service-account
key, git-tracked in plaintext. Rotate it in Google Cloud console → IAM → Service
accounts, update the site's Railway env (`LOWERBACK_PLAY_SA_B64`) with the new
key, then remove the file from the repo and its history.
