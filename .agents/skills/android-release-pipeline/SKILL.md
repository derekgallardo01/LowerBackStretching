---
name: android-release-pipeline
description: Automates Android version bumping, code quality checks, release signing verification, and signed Android App Bundle (AAB) generation for Google Play Store releases. Use when preparing, signing, building, or uploading Android release artifacts.
---

# Android Release Pipeline Skill

This skill guides and automates building signed Android App Bundles (`.aab`) for phone (`:app`) and Wear OS (`:wear`) releases according to Google Play Store submission standards.

---

## Release Pre-Flight Checklist

1. **Verify Version Numbers**:
   - `android/app/build.gradle.kts` → check `versionCode` (always strictly increment; never reuse) and `versionName`.
   - `android/wear/build.gradle.kts` → check `versionCode` and `versionName`.
2. **Verify Signing Keystore**:
   - Verify `keystore.properties` exists at the repo root and points to a valid `upload-keystore.jks`.
3. **Verify Play Assets**:
   - 512×512 icon: `play-store-icon-512.png`
   - 1024×500 feature graphic: `feature-graphic-1024x500.png`
   - Phone & tablet screenshots: `screenshots/phone/`, `screenshots/tablet/`

---

## Execution Commands

### 1. Run Complete Test Suite & Code Quality Checks
```bash
./gradlew :core:test :app:testDebugUnitTest :wear:testDebugUnitTest :app:lintDebug :wear:lintRelease --console=plain
```

### 2. Build Signed Release Bundles
Execute the release bundle task from the `android/` directory or repo root:
```bash
./gradlew :app:bundleRelease :wear:bundleRelease --console=plain
```

### 3. Output Artifact Locations
- **Phone App Bundle**: `android/app/build/outputs/bundle/release/app-release.aab`
- **Wear OS App Bundle**: `android/wear/build/outputs/bundle/release/wear-release.aab`

---

## Troubleshooting

- **Unsigned Bundle**: If Gradle completes without error but outputs an unsigned bundle, verify that `keystore.properties` is in the repository root directory (one level above `android/`).
- **R8 / ProGuard Minification Warnings**: Minification is enabled by default (`isMinifyEnabled = true`). Proguard rules are defined in `android/app/proguard-rules.pro` and `android/wear/proguard-rules.pro`.
