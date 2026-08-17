---
name: health-compliance-checker
description: Audits Android Health Connect permissions and iOS HealthKit capabilities against Google Play Minimum Scope and Apple App Store privacy policies to prevent policy rejections. Use when touching health integration code, manifests, entitlements, or privacy policies.
---

# Health Compliance & Privacy Checker Skill

This skill enforces strict adherence to Google Play Store Minimum Scope policies and Apple App Store HealthKit review guidelines.

---

## Core Privacy Architecture Rule

> [!IMPORTANT]
> **Strict Write-Only Policy**: The app logs completed stretching routines as workout/exercise sessions. It must **never** request read permissions (e.g. step counts, sleep data, heart rate, or user health history).

---

## Audit Checklist

### 1. Android Manifest Audit (`android/app/src/main/AndroidManifest.xml`)
Verify that:
- [x] Only `android.permission.health.WRITE_EXERCISE` or `androidx.health.permission.HealthDataHistory` (for insert) is declared.
- [x] No `READ_STEPS`, `READ_HEART_RATE`, or other read permissions are present.
- [x] Health Connect rationale activity intent filters are properly declared.

### 2. iOS Info.plist & Entitlements Audit (`ios/LowerBackStretching/Info.plist`)
Verify that:
- [x] `NSHealthUpdateUsageDescription` is present with clear user-facing text (e.g., *"Logs your stretching sessions as flexibility workouts in Apple Health.*").
- [x] `NSHealthShareUsageDescription` is **OMITTED** (ensures no read prompt is triggered by iOS).

### 3. Privacy Policy Parity (`PRIVACY.md`)
Verify that the published privacy policy at `https://derekgallardo01.github.io/LowerBackStretching/PRIVACY` accurately reflects write-only data behavior and zero collection of personal biometric records.
