# Audio assets

`AudioController` looks these files up at play time via
`Bundle.main.url(forResource:withExtension:)`, trying `.mp3`, `.m4a`, and
`.caf` in that order. If a file is missing the controller silently
falls back to no-op (the feature just plays nothing).

## Required files

| Resource          | Suggested length | Notes                       |
| ----------------- | ---------------- | --------------------------- |
| `music_calm.mp3`  | 1-3 min, loops   | Soft instrumental           |
| `music_lofi.mp3`  | 1-3 min, loops   | Lofi beats                  |
| `music_piano.mp3` | 1-3 min, loops   | Solo piano                  |
| `ambient_rain.mp3`   | 1-3 min, loops | Steady rain, no thunder     |
| `ambient_forest.mp3` | 1-3 min, loops | Birds + leaves              |
| `ambient_ocean.mp3`  | 1-3 min, loops | Waves on a beach            |
| `chime_bell.mp3`  | < 1s, one-shot   | Soft tonal bell             |
| `chime_ding.mp3`  | < 1s, one-shot   | Bright ding                 |
| `chime_drop.mp3`  | < 1s, one-shot   | Water drop                  |

The Android side uses the exact same filenames in `res/raw/` (no file
extension in the lookup). Keep both folders in lockstep.

Keep the total combined size under ~10 MB to avoid bloating the IPA.
