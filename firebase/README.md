# Firebase

The cloud backend for cross-device sync, anonymous auth, friend
leaderboards, and (future) workout-buddy mode. Wave 8 sets up
the scaffold; the mobile clients wire up sync in a follow-up commit.

## Layout

| File / dir | What it does |
| --- | --- |
| `firebase.json` | Top-level config — points to rules, indexes, functions, emulator ports. |
| `firestore.rules` | Security rules. Per-user data is locked to the owning UID; buddy rooms gated on a participants array. |
| `firestore.indexes.json` | Composite indexes (currently just `sessions` ordered by `completedAtEpochMillis`). |
| `functions/` | Cloud Functions (Node 20). Stubs for `aggregatePublicStats` and `cleanupBuddyRooms`. |

## First-time setup

1. **Create the Firebase project.** Console → Add project → pick a
   name like `lower-back-stretching`. Disable Google Analytics if
   you don't need it.
2. **Enable Authentication.** Authentication → Get started → Sign-in
   method → enable **Anonymous**. Optionally also enable Email/Password
   and Google so users can upgrade later.
3. **Enable Firestore.** Build → Firestore Database → Create database
   → Production mode → pick a region (us-central1 is the default).
4. **Install the CLI** (one-time, machine-wide):
   ```sh
   npm install -g firebase-tools
   firebase login
   ```
5. **Wire this directory to the project.** From this `firebase/` dir:
   ```sh
   firebase use --add
   ```
   Pick the project you just created; alias it `default`.

## Day-to-day commands

Run them from this directory.

```sh
# Deploy security rules and indexes:
firebase deploy --only firestore:rules,firestore:indexes

# Deploy Cloud Functions (Node deps must be installed first):
cd functions && npm install && cd ..
firebase deploy --only functions

# Deploy everything in one go:
firebase deploy

# Local emulator (auth + Firestore + Functions UI on 4000):
firebase emulators:start
```

## Data model

The mobile clients write to these paths:

```
/users/{uid}                              (user profile)
  /sessions/{sessionId}                   (each completed routine)
  /routines/{routineId}                   (custom routines synced cross-device)
  /programProgress/{programId}            (per-program "next day" bookmark)
  /flexibilityTests/{testId}              (self-test snapshots)

/buddyRooms/{roomId}                      (workout-buddy shared session — Wave 8 follow-up)
```

All collections under `/users/{uid}` are read/write-locked to the
owning UID via `firestore.rules`. Anonymous accounts work the same
way as upgraded ones — the rules don't distinguish.

## Linking the mobile apps

Once the project is created and `firebase use` is set up, drop the
generated config files into the platform projects:

- **Android**: download `google-services.json` from
  Firebase Console → Project settings → Your apps → Android → copy
  to `android/app/google-services.json` (gitignored). Add the
  `com.google.gms:google-services` Gradle plugin and the Firebase
  BoM dependency — see `android/README.md`.
- **iOS**: download `GoogleService-Info.plist` from
  Firebase Console → Project settings → Your apps → iOS → copy
  to the Xcode project root and add to the app target. Install the
  Firebase Swift Package Manager dependency — see `ios/README.md`.

Neither file is checked into git.
