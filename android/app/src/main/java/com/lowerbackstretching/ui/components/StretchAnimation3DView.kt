package com.lowerbackstretching.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.core.SKELETON_BONES
import com.lowerbackstretching.core.interpolatedPose
import com.lowerbackstretching.core.model.StretchAnimationSpec
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D-styled mannequin renderer. Same pose data as [StretchAnimationView];
 * the difference is purely cosmetic:
 *  - Each bone is drawn as a thick capsule (wide stroke + round caps) with
 *    a vertical lightness gradient that fakes a single overhead light
 *  - The head is a sphere drawn as a radial gradient circle (light on top,
 *    dark on bottom)
 *  - The whole figure is rotated slightly around its vertical axis so it
 *    reads as 3/4-perspective rather than flat profile
 *  - Bones further from the camera (after the Y-axis rotation) render with
 *    a slight alpha/desaturation so depth is visible
 *
 * Pose joints with only `[x, y]` are treated as `z = 0`. Future 3D pose
 * data can supply per-joint depth; this renderer projects to 2D at the end.
 */
@Composable
fun StretchAnimation3DView(
    animation: StretchAnimationSpec?,
    youtubeId: String,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(surface),
    ) {
        if (animation != null && animation.poses.isNotEmpty()) {
            Mannequin(
                spec = animation,
                bodyColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Fallback to the 2D placeholder — no animation data to render.
            // The user-facing copy and "Watch demo" link live in the 2D file.
            StretchAnimationView(
                animation = null,
                youtubeId = youtubeId,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Mannequin(
    spec: StretchAnimationSpec,
    bodyColor: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "stretch3d")
    val durationMs = (spec.loopSeconds * 1000).toInt().coerceAtLeast(250)
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = spec.poses.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "poseT",
    )
    val pose = interpolatedPose(spec.poses, t)
    Canvas(modifier = modifier) {
        drawMannequin(pose, bodyColor)
    }
}

/** How far around the vertical axis to rotate the figure for the 3/4 look,
 *  in radians. Small enough that side-view stretches still read clearly. */
private const val CAMERA_Y_ROTATION_RAD = 0.35f // ~20°

/** Apparent "camera distance" used by the perspective projection. Higher
 *  = less perspective distortion. */
private const val CAMERA_DISTANCE = 4f

private fun DrawScope.drawMannequin(
    joints: Map<String, Pair<Float, Float>>,
    bodyColor: Color,
) {
    if (joints.isEmpty()) return
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    // Project each joint: normalized [0,1] -> centered NDC [-1,+1] -> rotate
    // around Y axis -> perspective divide -> back to pixel coords.
    val projected = mutableMapOf<String, ProjectedJoint>()
    for ((name, p) in joints) {
        val (nx, ny) = p
        // Center the figure horizontally on its own bounding-box midpoint
        // (we use the canvas center) and convert y into a "up is +" frame
        // so the figure renders right-side-up under rotation.
        val x = (nx - 0.5f)
        val y = -(ny - 0.5f)
        val z = 0f
        val rotX = x * cos(CAMERA_Y_ROTATION_RAD) + z * sin(CAMERA_Y_ROTATION_RAD)
        val rotZ = -x * sin(CAMERA_Y_ROTATION_RAD) + z * cos(CAMERA_Y_ROTATION_RAD)
        val scale = CAMERA_DISTANCE / (CAMERA_DISTANCE + rotZ)
        val px = cx + rotX * scale * size.minDimension
        val py = cy + (-y) * scale * size.minDimension
        projected[name] = ProjectedJoint(Offset(px, py), depth = rotZ, scale = scale)
    }

    val baseStroke = (size.minDimension * 0.045f).coerceAtLeast(8f)
    val headRadius = size.minDimension * 0.06f

    // Z-sort bones so far ones draw first.
    val bones = SKELETON_BONES.mapNotNull { (a, b) ->
        val pa = projected[a] ?: return@mapNotNull null
        val pb = projected[b] ?: return@mapNotNull null
        Triple(pa, pb, (pa.depth + pb.depth) / 2f)
    }.sortedByDescending { it.third }

    for ((pa, pb, _) in bones) {
        val avgScale = (pa.scale + pb.scale) / 2f
        val strokePx = baseStroke * avgScale
        val a = pa.point
        val b = pb.point
        val depthBlend = ((pa.depth + pb.depth) / 2f + 0.5f).coerceIn(0f, 1f)
        val tint = lerpColor(bodyColor, Color(0xFF1B3A6E), depthBlend * 0.5f)
        drawLine(
            color = tint,
            start = a,
            end = b,
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
        val highlight = lerpColor(tint, Color.White, 0.35f)
        drawLine(
            color = highlight.copy(alpha = 0.45f),
            start = a,
            end = b,
            strokeWidth = strokePx * 0.35f,
            cap = StrokeCap.Round,
        )
    }

    // Head — drawn as a radial gradient sphere with shading.
    projected["head"]?.let { h ->
        val r = headRadius * h.scale
        val center = h.point
        val grad = Brush.radialGradient(
            colors = listOf(
                lerpColor(bodyColor, Color.White, 0.4f),
                bodyColor,
                lerpColor(bodyColor, Color(0xFF0D2545), 0.6f),
            ),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.4f),
            radius = r * 1.6f,
        )
        drawCircle(brush = grad, radius = r, center = center)
    }
}

private data class ProjectedJoint(
    val point: Offset,
    val depth: Float,
    val scale: Float,
)

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val s = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * s,
        green = a.green + (b.green - a.green) * s,
        blue = a.blue + (b.blue - a.blue) * s,
        alpha = a.alpha + (b.alpha - a.alpha) * s,
    )
}

