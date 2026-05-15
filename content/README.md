# Content

Two JSON files drive all stretching content shown in the apps:

- `stretches.json` — catalog of individual stretches. Each has a YouTube video
  ID, duration, body parts, and a short description.
- `programs.json` — multi-day programs that reference stretches by id.

## YouTube IDs

The `youtubeId` field is the 11-character ID from a YouTube URL, e.g. for
`https://www.youtube.com/watch?v=dQw4w9WgXcQ` the id is `dQw4w9WgXcQ`.

All IDs in this repo start with `PLACEHOLDER_` and **must be replaced** before
the app is useful. Suggested curation process:

1. Search YouTube for the stretch name (e.g. "child's pose tutorial").
2. Pick a video from a reputable physiotherapy / yoga channel with a permissive
   embedding policy (check the channel — most allow embeds by default).
3. Copy the 11-char video ID into the corresponding stretch entry.
4. Verify in the app that the embed plays (some channels disable embedding;
   you'll see "video unavailable" — pick another).

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
