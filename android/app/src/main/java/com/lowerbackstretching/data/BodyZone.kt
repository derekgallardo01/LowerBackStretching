package com.lowerbackstretching.data

/**
 * A tappable region on the body-diagram silhouette. Each zone maps to
 * a `bodyParts` tag that already appears in stretches.json — tapping
 * the zone surfaces every stretch that includes the tag.
 *
 * Coordinates are normalized to the silhouette's 0..1 bounding box,
 * so the same data drives every screen size. The silhouette is drawn
 * as a back-view single figure; left/right limbs share the same row
 * (HAMSTRINGS / CALVES rectangles span both legs).
 */
enum class BodyZone(
    val displayName: String,
    val bodyPartTag: String,
    val rect: NormalizedRect,
) {
    NECK       ("Neck",        "spine",       NormalizedRect(0.42f, 0.04f, 0.16f, 0.06f)),
    UPPER_BACK ("Upper back",  "upper-back",  NormalizedRect(0.30f, 0.16f, 0.40f, 0.12f)),
    LOWER_BACK ("Lower back",  "lower-back",  NormalizedRect(0.32f, 0.32f, 0.36f, 0.12f)),
    HIPS       ("Hips",        "hips",        NormalizedRect(0.27f, 0.44f, 0.46f, 0.07f)),
    GLUTES     ("Glutes",      "glutes",      NormalizedRect(0.30f, 0.51f, 0.40f, 0.10f)),
    HAMSTRINGS ("Hamstrings",  "hamstrings",  NormalizedRect(0.30f, 0.63f, 0.40f, 0.15f)),
    CALVES     ("Calves",      "calves",      NormalizedRect(0.32f, 0.80f, 0.36f, 0.12f)),
}

/** Rectangle in [0..1] x [0..1] silhouette space. */
data class NormalizedRect(val x: Float, val y: Float, val w: Float, val h: Float)

/**
 * Map a stretch's free-form `bodyParts` tags to the structured
 * [BodyZone] set the silhouette knows how to draw. Multiple tags can
 * highlight the same zone ("spine" lights up neck, upper back, and
 * lower back); unknown tags ("core", "groin", "quads") are silently
 * dropped because no zone exists for them yet.
 */
fun bodyZonesForTags(tags: List<String>): Set<BodyZone> {
    val zones = mutableSetOf<BodyZone>()
    for (tag in tags) when (tag) {
        "spine" -> {
            zones += BodyZone.NECK
            zones += BodyZone.UPPER_BACK
            zones += BodyZone.LOWER_BACK
        }
        "upper-back" -> zones += BodyZone.UPPER_BACK
        "lower-back" -> zones += BodyZone.LOWER_BACK
        "hips", "groin" -> zones += BodyZone.HIPS
        "glutes" -> zones += BodyZone.GLUTES
        "hamstrings" -> zones += BodyZone.HAMSTRINGS
        "calves" -> zones += BodyZone.CALVES
        // "core", "quads" — intentionally unmapped; front-view-only zones.
    }
    return zones
}
