---
name: content-sync
description: Validates and synchronizes shared JSON assets (stretches, programs, glossary) across the source directory, Android assets, and iOS resources. Use when adding new stretches, modifying routines, updating glossary items, or verifying platform data parity.
---

# Content Synchronization & Validation Skill

This skill ensures that all shared JSON data (`stretches.json`, `programs.json`, and `glossary.json`) remains strictly validated and synchronized between `content/`, the Android assets folder, and the iOS resources folder.

## File Locations

| Asset | Source | Android Phone Target | iOS Target |
| :--- | :--- | :--- | :--- |
| **Stretches** | `content/stretches.json` | `android/app/src/main/assets/stretches.json` | `ios/LowerBackStretching/Resources/stretches.json` |
| **Programs** | `content/programs.json` | `android/app/src/main/assets/programs.json` | `ios/LowerBackStretching/Resources/programs.json` |
| **Glossary** | `content/glossary.json` | `android/app/src/main/assets/glossary.json` | `ios/LowerBackStretching/Resources/glossary.json` |

---

## Workflow Steps

### 1. Check Content Status & Drift
Run the helper script with `--check` to verify schemas and detect any unsynchronized files:

```bash
python .agents/skills/content-sync/scripts/sync_content.py --check
```

### 2. Apply Synchronization
Whenever files in `content/` are added or updated, propagate the changes across all targets:

```bash
python .agents/skills/content-sync/scripts/sync_content.py --apply
```

### 3. Verify Unit Tests
After updating content files, verify that the core player engine and repositories load the updated data without serialization errors:

```bash
./gradlew :core:test :app:testDebugUnitTest --console=plain
```
