---
name: ios-project-sync
description: Manages iOS Xcode project settings, SwiftData schema compatibility, entitlements, and compilation verification for iOS and watchOS targets. Use when verifying iOS code, adding Swift files, running tests, or updating project.yml.
---

# iOS Project Sync & Build Verification Skill

This skill guides maintaining iOS (SwiftUI + SwiftData) and watchOS companion code in sync with the Android core engine and assets.

---

## Key Files & Structure

- **XcodeGen Spec**: `ios/project.yml`
- **Phone App Source**: `ios/LowerBackStretching/`
- **Watch App Source**: `ios/WatchApp/`
- **Shared Resources**: `ios/LowerBackStretching/Resources/` (`stretches.json`, `programs.json`, `glossary.json`)
- **App Entitlements**: `ios/LowerBackStretching/LowerBackStretching.entitlements`

---

## Workflow Steps

### 1. Generating or Updating Xcode Project (`project.yml`)
If Xcode project file generation via XcodeGen is used:
```bash
cd ios && xcodegen generate
```

### 2. Required Info.plist Usage Descriptions
Ensure the following keys exist:
- `NSHealthUpdateUsageDescription`: "Logs your stretching sessions as flexibility workouts in Apple Health."
- `NSCalendarsWriteOnlyAccessUsageDescription`: "Adds a stretching break event to your calendar."
- `CFBundleURLTypes` -> `lowerbackstretching` (Custom URL scheme for routine import)

### 3. Running iOS Tests via CLI (macOS / CI)
```bash
xcodebuild test \
  -scheme LowerBackStretching \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'
```
