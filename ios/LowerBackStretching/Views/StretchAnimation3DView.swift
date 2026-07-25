import SwiftUI

/// 3D-styled mannequin renderer. Same pose data as `StretchAnimationView`;
/// the difference is purely cosmetic:
///  - Each bone is drawn as a thick capsule (wide stroke with rounded caps)
///  - The whole figure is rotated slightly around its vertical axis so it
///    reads as 3/4-perspective rather than flat profile
///  - Bones further from the camera (after the Y-axis rotation) render
///    slightly darker so depth is visible
///  - The head is a radial-gradient sphere with shading
///
/// Pose joints with only `[x, y]` are treated as `z = 0`. Future 3D pose
/// data can supply per-joint depth.
struct StretchAnimation3DView: View {
    let animation: StretchAnimationSpec?
    let youtubeId: String

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.secondarySystemBackground))

            if let animation, !animation.poses.isEmpty {
                Mannequin(spec: animation)
            } else {
                // Fallback: 2D placeholder owns the "follow the timer" copy.
                StretchAnimationView(animation: nil, youtubeId: youtubeId)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct Mannequin: View {
    let spec: StretchAnimationSpec

    var body: some View {
        TimelineView(.animation) { context in
            let secs = context.date.timeIntervalSinceReferenceDate
            let loop = max(spec.loopSeconds, 0.25)
            let phase = secs.truncatingRemainder(dividingBy: loop) / loop
            let t = phase * Double(spec.poses.count)
            let joints = StretchAnimationView.interpolatedPose(poses: spec.poses, t: t)
            Canvas { ctx, size in
                drawMannequin(joints: joints, in: ctx, size: size)
            }
        }
    }
}

private let cameraYRotation: Double = 0.35   // ~20°
private let cameraDistance: Double = 4.0

private struct ProjectedJoint {
    let point: CGPoint
    let depth: Double
    let scale: Double
}

private func drawMannequin(joints: [String: CGPoint], in ctx: GraphicsContext, size: CGSize) {
    if joints.isEmpty { return }
    let w = size.width
    let h = size.height
    let cx = w / 2
    let cy = h / 2
    let minDim = min(w, h)

    var projected: [String: ProjectedJoint] = [:]
    for (name, p) in joints {
        let x = Double(p.x) - 0.5
        let y = -(Double(p.y) - 0.5)
        let z = 0.0
        let rotX = x * cos(cameraYRotation) + z * sin(cameraYRotation)
        let rotZ = -x * sin(cameraYRotation) + z * cos(cameraYRotation)
        let scale = cameraDistance / (cameraDistance + rotZ)
        let px = Double(cx) + rotX * scale * Double(minDim)
        let py = Double(cy) + (-y) * scale * Double(minDim)
        projected[name] = ProjectedJoint(
            point: CGPoint(x: px, y: py),
            depth: rotZ,
            scale: scale
        )
    }

    let baseStroke = max(8.0, Double(minDim) * 0.045)
    let headRadius = Double(minDim) * 0.06

    // Z-sort bones (far first).
    let bones = StretchAnimationView.skeletonBones.compactMap { pair -> (ProjectedJoint, ProjectedJoint, Double)? in
        let (aName, bName) = pair
        guard let pa = projected[aName], let pb = projected[bName] else { return nil }
        return (pa, pb, (pa.depth + pb.depth) / 2.0)
    }.sorted { $0.2 > $1.2 }

    // Same dark-blue shading constants as Android's StretchAnimation3DView
    // and the JS preview, so all three renderers produce visually identical
    // depth cues. The previous iOS implementation reduced opacity for depth,
    // which gave a washed-out look that didn't match Android.
    let bodyColor = Color.accentColor
    let depthDark = Color(red: 27 / 255, green: 58 / 255, blue: 110 / 255)
    let headDark = Color(red: 13 / 255, green: 37 / 255, blue: 69 / 255)

    for (pa, pb, _) in bones {
        let avgScale = (pa.scale + pb.scale) / 2.0
        let stroke = baseStroke * avgScale
        let depthBlend = max(0.0, min(1.0, (pa.depth + pb.depth) / 2.0 + 0.5))

        var path = Path()
        path.move(to: pa.point)
        path.addLine(to: pb.point)

        // Main capsule: blend bone color toward dark-blue with depth.
        let tint = bodyColor.blended(with: depthDark, fraction: depthBlend * 0.5)
        ctx.stroke(
            path,
            with: .color(tint),
            style: StrokeStyle(lineWidth: stroke, lineCap: .round)
        )

        // Highlight stripe: tint blended toward white at 45% alpha.
        let highlight = tint.blended(with: .white, fraction: 0.35).opacity(0.45)
        ctx.stroke(
            path,
            with: .color(highlight),
            style: StrokeStyle(lineWidth: stroke * 0.35, lineCap: .round)
        )
    }

    // Head as a shaded sphere via radial gradient.
    if let head = projected["head"] {
        let r = headRadius * head.scale
        let rect = CGRect(
            x: head.point.x - r,
            y: head.point.y - r,
            width: r * 2,
            height: r * 2
        )
        let gradient = Gradient(colors: [
            bodyColor.blended(with: .white, fraction: 0.4),
            bodyColor,
            bodyColor.blended(with: headDark, fraction: 0.6),
        ])
        ctx.fill(
            Path(ellipseIn: rect),
            with: .radialGradient(
                gradient,
                center: CGPoint(x: head.point.x - r * 0.3, y: head.point.y - r * 0.4),
                startRadius: 0,
                endRadius: r * 1.6
            )
        )
    }
}

// SwiftUI's Color exposes no public component accessor, so blending goes
// through UIColor. This works on all supported iOS versions (no iOS-18-only
// `Color.mix` API needed).
private extension Color {
    func blended(with other: Color, fraction: Double) -> Color {
        let f = CGFloat(max(0.0, min(1.0, fraction)))
        var r1: CGFloat = 0, g1: CGFloat = 0, b1: CGFloat = 0, a1: CGFloat = 0
        var r2: CGFloat = 0, g2: CGFloat = 0, b2: CGFloat = 0, a2: CGFloat = 0
        UIColor(self).getRed(&r1, green: &g1, blue: &b1, alpha: &a1)
        UIColor(other).getRed(&r2, green: &g2, blue: &b2, alpha: &a2)
        return Color(
            red: Double(r1 + (r2 - r1) * f),
            green: Double(g1 + (g2 - g1) * f),
            blue: Double(b1 + (b2 - b1) * f),
            opacity: Double(a1 + (a2 - a1) * f)
        )
    }
}
