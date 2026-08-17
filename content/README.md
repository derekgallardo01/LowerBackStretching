# Content

Two JSON files drive all stretching content shown in the apps:

- `stretches.json` — catalog of individual stretches. Each has a YouTube video
  ID, duration, body parts, and a short description.
- `programs.json` — multi-day programs that reference stretches by id.

## YouTube IDs

The `youtubeId` field is the 11-character ID from a YouTube URL, e.g. for
`https://www.youtube.com/watch?v=dQw4w9WgXcQ` the id is `dQw4w9WgXcQ`.

YouTube is **not** embedded in the player anymore — the mid-2025 embedder
verification tightening (Error 152) made WebView embeds unreliable. Each
stretch now ships with a built-in stick-figure `animation` (see below) and
the YouTube ID powers a small "Watch demo" link that opens the video in
the YouTube app or browser.

A leading `PLACEHOLDER_` (or an empty string) signals "no demo video yet"
and suppresses the link entirely.

## Animation poses

Each stretch can ship a looping stick-figure animation that demonstrates
the motion. The renderer is a tiny Compose / SwiftUI Canvas that draws line
segments between named joints.

```json
"animation": {
  "loopSeconds": 4.5,
  "poses": [
    {
      "name": "cow",
      "joints": {
        "head":     [0.80, 0.33],
        "neck":     [0.72, 0.40],
        "shoulder": [0.65, 0.45],
        "elbow":    [0.65, 0.65],
        "hand":     [0.65, 0.85],
        "spineMid": [0.475, 0.58],
        "hip":      [0.30, 0.45],
        "knee":     [0.30, 0.85],
        "foot":     [0.20, 0.85]
      }
    },
    { "name": "cat", "joints": { ... } }
  ]
}
```

- `loopSeconds`: time for one full loop through all poses.
- `poses`: 2+ keyframes. The renderer eases between consecutive poses and
  wraps back to the first after the last (so `[A, B]` plays `A → B → A → …`).
- `name` is optional, for human orientation when reading the JSON.
- `joints` keys the renderer understands: `head`, `neck`, `shoulder`,
  `elbow`, `hand`, `spineMid`, `hip`, `knee`, `foot`. All poses in one
  spec should declare the same joints.
- `[x, y]` are normalized to the drawing surface: `[0, 0]` is top-left,
  `[1, 1]` is bottom-right.

Bones connecting consecutive joints (`head→neck`, `neck→spineMid`,
`spineMid→hip`, `neck→shoulder`, `shoulder→elbow`, `elbow→hand`,
`hip→knee`, `knee→foot`) are drawn as line segments; the head is a
small circle at `head`.

Stretches without `animation` show a placeholder + "Watch demo" link.

## Keeping platforms in sync

The same JSON files need to live in two places for the apps to load them as
bundled assets:

- `android/app/src/main/assets/stretches.json` (and `programs.json`)
- `ios/LowerBackStretching/Resources/stretches.json` (and `programs.json`)

The canonical copies are here in `content/`. A simple sync script:

```sh
cp content/stretches.json android/app/src/main/assets/stretches.json
cp content/programs.json   android/app/src/main/assets/programs.json
cp content/stretches.json ios/LowerBackStretching/Resources/stretches.json
cp content/programs.json  ios/LowerBackStretching/Resources/programs.json
```

## Schema

### Stretch

```json
{
  "id": "cat-cow",
  "name": "Cat-Cow",
  "bodyParts": ["lower-back", "spine"],
  "durationSeconds": 45,
  "difficulty": "easy",
  "description": "Alternate arching and rounding your spine on hands and knees.",
  "youtubeId": "PLACEHOLDER_CAT_COW"
}
```

`bodyParts` is a free-form list — used for filtering. Conventional values:
`lower-back`, `upper-back`, `spine`, `hips`, `glutes`, `hamstrings`,
`quads`, `calves`, `groin`, `neck`.

`difficulty` is one of `easy`, `medium`, `hard`.

### Video demonstrations

Stretches can include a `videoUrl` � an HTTPS URL to a short looping MP4
(5�15 seconds, silent, portrait or landscape). When present, both the
Android and iOS players render the video instead of the stick-figure
animation. If the video fails to load or is absent, the app falls back
to the `animation` or placeholder automatically.

```json
{
  "id": "cat-cow",
  "videoUrl": "https://cdn.example.com/stretches/cat-cow.mp4"
}
```

Hosting recommendations:
- Encode as H.264 MP4 for maximum compatibility
- Keep under 2 MB per stretch (26 stretches � 2 MB = 52 MB total)
- Host on a CDN with CORS headers allowing your app domain
- Loop seamlessly by matching first and last frames

### Program

```json
{
  "id": "lower-back-relief-7day",
  "title": "Lower Back Relief — 7 Days",
  "category": "lower-back",
  "summary": "A gentle week to ease tension in the lumbar spine.",
  "days": [
    { "day": 1, "title": "Gentle Start", "stretchIds": ["cat-cow", "child-pose"] }
  ]
}
```

`category` conventional values: `lower-back`, `legs`, `hips`, `flexibility`,
`sciatica`, `posture`.
