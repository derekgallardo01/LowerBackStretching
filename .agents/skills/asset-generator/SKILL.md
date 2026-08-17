---
name: asset-generator
description: Generates, renders, and injects visual store assets, adaptive icons, feature graphics, Lottie vector animations, and stretch demo video clips using Python scripts. Use when updating app branding, regenerating Play Store / App Store graphics, or rebuilding stretch animations.
---

# Asset Generation & Media Pipeline Skill

This skill documents and executes the automated Python asset generation scripts located in `scripts/`.

---

## Tool Scripts & Workflows

### 1. Play Store 512×512 Icon
Renders the adaptive icon vectors onto a 512×512 PNG canvas:
```bash
python scripts/generate_play_store_icon.py
# Output: play-store-icon-512.png
```

### 2. Feature Graphic (1024×500)
Renders the promotional banner for Google Play:
```bash
python scripts/generate_feature_graphic.py
# Output: feature-graphic-1024x500.png
```

### 3. Android Adaptive & Mipmap Icons
Generates full density mipmap sets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`):
```bash
python scripts/generate_android_icons.py
```

### 4. Vector & Lottie Animation Injection
Injects or updates custom stick-figure vector animations into `content/stretches.json`:
```bash
python scripts/inject_animations.py
```

### 5. Stretch Video Demonstration Clips
Renders 2D stick-figure video demonstrations into MP4 format:
```bash
python scripts/generate_all_stretch_videos.py
```
