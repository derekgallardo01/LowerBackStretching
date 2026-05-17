package com.lowerbackstretching.ui.anatomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.core.BodyZone

/**
 * Back-view body silhouette drawn purely from primitive shapes (no
 * bundled SVG). Renders in a 1:2 aspect-ratio box so coordinates stay
 * consistent across screen sizes.
 *
 * Pass [onZoneTap] for the interactive (find-a-stretch) mode, or pass
 * [highlightedZones] for the read-only (player anatomy overlay) mode.
 * Both can be active simultaneously.
 */
@Composable
fun BodySilhouette(
    modifier: Modifier = Modifier,
    onZoneTap: ((BodyZone) -> Unit)? = null,
    highlightedZones: Set<BodyZone> = emptySet(),
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
    highlightColor: Color = MaterialTheme.colorScheme.primary,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.5f),
    ) {
        val widthDp = maxWidth
        val heightDp = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackSilhouette(size = size, color = tint)
            for (zone in highlightedZones) {
                drawZoneFill(zone, size, highlightColor.copy(alpha = 0.45f))
            }
        }
        if (onZoneTap != null) {
            BodyZone.entries.forEach { zone ->
                val r = zone.rect
                Box(
                    modifier = Modifier
                        .size(widthDp * r.w, heightDp * r.h)
                        .offset(widthDp * r.x, heightDp * r.y)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics { contentDescription = zone.displayName }
                        .clickable(role = Role.Button) { onZoneTap(zone) },
                )
            }
        }
    }
}

private fun DrawScope.drawBackSilhouette(size: Size, color: Color) {
    val w = size.width
    val h = size.height
    // Head — circle near the top.
    drawCircle(
        color = color,
        radius = w * 0.11f,
        center = Offset(x = w * 0.5f, y = h * 0.07f),
    )
    // Neck — short rectangle.
    drawRoundedBlock(color, w * 0.45f, h * 0.12f, w * 0.10f, h * 0.04f, w * 0.02f)
    // Torso — tapered hexagon from shoulders down to hips.
    val torso = Path().apply {
        moveTo(w * 0.20f, h * 0.16f)
        lineTo(w * 0.80f, h * 0.16f)
        lineTo(w * 0.72f, h * 0.45f)
        lineTo(w * 0.78f, h * 0.52f)
        lineTo(w * 0.22f, h * 0.52f)
        lineTo(w * 0.28f, h * 0.45f)
        close()
    }
    drawPath(torso, color)
    // Arms — slim rectangles flanking the torso.
    drawRoundedBlock(color, w * 0.04f, h * 0.17f, w * 0.13f, h * 0.30f, w * 0.05f)
    drawRoundedBlock(color, w * 0.83f, h * 0.17f, w * 0.13f, h * 0.30f, w * 0.05f)
    // Glutes — rounded rectangle below the hips.
    drawRoundedBlock(color, w * 0.24f, h * 0.50f, w * 0.52f, h * 0.13f, w * 0.06f)
    // Legs — two tapered rectangles down to ankles.
    val leftLeg = Path().apply {
        moveTo(w * 0.26f, h * 0.60f)
        lineTo(w * 0.46f, h * 0.60f)
        lineTo(w * 0.44f, h * 0.94f)
        lineTo(w * 0.30f, h * 0.94f)
        close()
    }
    val rightLeg = Path().apply {
        moveTo(w * 0.54f, h * 0.60f)
        lineTo(w * 0.74f, h * 0.60f)
        lineTo(w * 0.70f, h * 0.94f)
        lineTo(w * 0.56f, h * 0.94f)
        close()
    }
    drawPath(leftLeg, color)
    drawPath(rightLeg, color)
}

private fun DrawScope.drawZoneFill(zone: BodyZone, size: Size, color: Color) {
    val r = zone.rect
    drawRoundedBlock(
        color = color,
        x = r.x * size.width,
        y = r.y * size.height,
        w = r.w * size.width,
        h = r.h * size.height,
        corner = size.width * 0.02f,
    )
}

private fun DrawScope.drawRoundedBlock(
    color: Color,
    x: Float, y: Float, w: Float, h: Float, corner: Float,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner),
    )
}
