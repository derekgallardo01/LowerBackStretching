# Audio assets

`AudioController` looks up these files at play time via
`resources.getIdentifier(name, "raw", packageName)`. If a file is missing
the controller silently falls back to no-op (build still succeeds; the
feature just plays nothing).

## Required files

All files must be MP3 (Android also accepts OGG and WAV; MP3 is the
cross-platform default).

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

Loop files should be encoded as gapless MP3s — Android's `MediaPlayer`
re-seeks to zero on `isLooping=true`, which can introduce a small gap on
poorly-mastered files. If gaps are noticeable, re-export at the same
bitrate with `lame --nogap`.

Keep the total combined size under ~10 MB to avoid bloating the APK.
