package com.lowerbackstretching.core

import com.lowerbackstretching.core.model.Pose
import com.lowerbackstretching.core.model.StretchAnimationSpec
import kotlin.math.cos

/*
 * Pure-math support for the looping stick-figure animation. UI modules
 * call [interpolatedPose] every frame and translate the result into
 * platform-specific drawing primitives.
 */

/** Bones connect consecutive joint pairs as line segments. Same order on
 *  every platform so authoring stays portable. The L-suffixed joints
 *  (`shoulderL`, `elbowL`, `handL`, `kneeL`, `footL`) are optional — only
 *  used by front-view poses that need to show both sides of the body.
 *  Side-view poses omit them and the renderer's `?: continue` skips. */
val SKELETON_BONES: List<Pair<String, String>> = listOf(
    "head" to "neck",
    "neck" to "spineMid",
    "spineMid" to "hip",
    "neck" to "shoulder",
    "shoulder" to "elbow",
    "elbow" to "hand",
    "hip" to "knee",
    "knee" to "foot",
    // Optional left side for front-view poses.
    "neck" to "shoulderL",
    "shoulderL" to "elbowL",
    "elbowL" to "handL",
    "hip" to "kneeL",
    "kneeL" to "footL",
)

/** Maps elapsed wall-clock seconds onto the half-open range `[0, poseCount)`,
 *  advancing through the full loop once every [StretchAnimationSpec.loopSeconds]. */
fun tValueForElapsed(
    elapsedSeconds: Double,
    spec: StretchAnimationSpec,
): Float {
    if (spec.poses.isEmpty()) return 0f
    val loop = if (spec.loopSeconds > 0) spec.loopSeconds else 1.0
    val phase = ((elapsedSeconds % loop) + loop) % loop / loop
    return (phase * spec.poses.size).toFloat()
}

/**
 * Interpolates between consecutive poses with an eased segment, wrapping
 * after the last pose. `t` is expected in `[0, poses.size)`; values outside
 * are wrapped via modulo so callers don't have to guard against drift.
 *
 * Easing is per-segment: each transition starts and ends slowly (`(1 - cos)/2`)
 * which reads as natural breath-paced motion. Joint names present in the
 * first pose but missing in the next are held in place.
 */
fun interpolatedPose(
    poses: List<Pose>,
    t: Float,
): Map<String, Pair<Float, Float>> {
    if (poses.isEmpty()) return emptyMap()
    val n = poses.size
    val wrapped = ((t.toDouble() % n) + n) % n
    val i = wrapped.toInt().coerceIn(0, n - 1)
    val j = (i + 1) % n
    val raw = (wrapped - i).toFloat()
    val eased = (0.5f - 0.5f * cos(raw * Math.PI.toFloat())).coerceIn(0f, 1f)
    val a = poses[i].joints
    val b = poses[j].joints
    val out = LinkedHashMap<String, Pair<Float, Float>>(a.size)
    for ((key, pa) in a) {
        if (pa.size < 2) continue
        val pb = b[key] ?: pa
        if (pb.size < 2) continue
        out[key] = Pair(
            lerp(pa[0].toFloat(), pb[0].toFloat(), eased),
            lerp(pa[1].toFloat(), pb[1].toFloat(), eased),
        )
    }
    return out
}

private fun lerp(
    a: Float,
    b: Float,
    t: Float,
): Float = a + (b - a) * t
