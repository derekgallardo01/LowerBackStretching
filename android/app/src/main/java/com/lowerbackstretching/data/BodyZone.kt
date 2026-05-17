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
