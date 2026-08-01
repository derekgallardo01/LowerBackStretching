import SwiftUI

/// The player surface that replaces the YouTube embed. If `animation` is
/// present we render a looping stick-figure that demonstrates the stretch;
/// otherwise we show a static placeholder. In both cases, if `youtubeId`
/// points to a real video we surface a small "Watch demo on YouTube" link
/// that opens the video in the YouTube app or browser — embedded playback
/// was removed because YouTube's mid-2025 embedder-verification tightening
/// (Error 152) made WebView-based embeds unreliable across devices.
struct StretchAnimationView: View {
    let animation: StretchAnimationSpec?
    let youtubeId: String

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.secondarySystemBackground))

            if let animation, !animation.poses.isEmpty {
                AnimatedFigure(spec: animation)
                if Self.hasWatchableVideo(youtubeId) {
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            WatchOnYouTubeChip(youtubeId: youtubeId)
                                .padding(8)
                        }
                    }
                }
            } else {
                NoAnimationPlaceholder(youtubeId: youtubeId)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    static func hasWatchableVideo(_ id: String) -> Bool {
        !id.isEmpty && !id.hasPrefix("PLACEHOLDER_")
    }
}

private struct AnimatedFigure: View {
    let spec: StretchAnimationSpec

    var body: some View {
        TimelineView(.animation) { context in
            let t = Self.tValue(
                date: context.date,
                loopSeconds: max(spec.loopSeconds, 0.25),
                poseCount: spec.poses.count
            )
            let joints = StretchAnimationView.interpolatedPose(poses: spec.poses, t: t)
            Canvas { ctx, size in
                StretchAnimationView.draw(joints: joints, in: ctx, size: size)
            }
        }
    }

    /// Maps wall-clock time onto `[0, poseCount)`, advancing through the
    /// loop at `loopSeconds` per full cycle.
    static func tValue(date: Date, loopSeconds: Double, poseCount: Int) -> Double {
        let secs = date.timeIntervalSinceReferenceDate
        let phase = secs.truncatingRemainder(dividingBy: loopSeconds) / loopSeconds
        return phase * Double(poseCount)
    }
}

extension StretchAnimationView {
    /// Bones connect consecutive joint pairs as line segments. The
    /// L-suffixed joints are optional — only used by front-view poses
    /// that need to show both sides of the body. Side-view poses omit
    /// them and the renderer's guard else continue skips.
    static let skeletonBones: [(String, String)] = [
        ("head", "neck"),
        ("neck", "spineMid"),
        ("spineMid", "hip"),
        ("neck", "shoulder"),
        ("shoulder", "elbow"),
        ("elbow", "hand"),
        ("hip", "knee"),
        ("knee", "foot"),
        // Optional left side for front-view poses.
        ("neck", "shoulderL"),
        ("shoulderL", "elbowL"),
        ("elbowL", "handL"),
        ("hip", "kneeL"),
        ("kneeL", "footL"),
    ]

    /// Interpolates between consecutive poses with an eased segment,
    /// wrapping after the last pose. `t` is expected in `[0, poses.count)`.
    static func interpolatedPose(poses: [Pose], t: Double) -> [String: CGPoint] {
        guard !poses.isEmpty else { return [:] }
        let n = poses.count
        let wrapped = (t.truncatingRemainder(dividingBy: Double(n)) + Double(n))
            .truncatingRemainder(dividingBy: Double(n))
        let i = min(max(Int(wrapped), 0), n - 1)
        let j = (i + 1) % n
        let raw = wrapped - Double(i)
        let eased = max(0.0, min(1.0, 0.5 - 0.5 * cos(raw * .pi)))
        let a = poses[i].joints
        let b = poses[j].joints
        var out: [String: CGPoint] = [:]
        for key in a.keys {
            guard let pa = a[key], pa.count >= 2 else { continue }
            let pb = b[key] ?? pa
            guard pb.count >= 2 else { continue }
            out[key] = CGPoint(
                x: lerp(pa[0], pb[0], eased),
                y: lerp(pa[1], pb[1], eased)
            )
        }
        return out
    }

    static func draw(joints: [String: CGPoint], in ctx: GraphicsContext, size: CGSize) {
        let w = size.width
        let h = size.height
        let minDim = min(w, h)
        let stroke = max(3.0, minDim * 0.018)
        let headRadius = minDim * 0.05
        let color = Color.accentColor

        let absolute: [String: CGPoint] = joints.mapValues { p in
            CGPoint(x: p.x * w, y: p.y * h)
        }

        var path = Path()
        for (aName, bName) in skeletonBones {
            guard let a = absolute[aName], let b = absolute[bName] else { continue }
            path.move(to: a)
            path.addLine(to: b)
        }
        ctx.stroke(
            path,
            with: .color(color),
            style: StrokeStyle(lineWidth: stroke, lineCap: .round)
        )

        if let head = absolute["head"] {
            let rect = CGRect(
                x: head.x - headRadius,
                y: head.y - headRadius,
                width: headRadius * 2,
                height: headRadius * 2
            )
            ctx.stroke(
                Path(ellipseIn: rect),
                with: .color(color),
                lineWidth: stroke
            )
        }
    }
}

private func lerp(_ a: Double, _ b: Double, _ t: Double) -> Double {
    a + (b - a) * t
}

private struct NoAnimationPlaceholder: View {
    let youtubeId: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "figure.cooldown")
                .font(.system(size: 36))
                .foregroundStyle(.secondary)
            Text("Follow the description and timer below.")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            if StretchAnimationView.hasWatchableVideo(youtubeId) {
                WatchOnYouTubeChip(youtubeId: youtubeId)
                    .padding(.top, 4)
            }
        }
        .padding(16)
    }
}

private struct WatchOnYouTubeChip: View {
    let youtubeId: String

    @Environment(\.openURL) private var openURL

    var body: some View {
        Button {
            if let url = URL(string: "https://www.youtube.com/watch?v=\(youtubeId)") {
                openURL(url)
            }
        } label: {
            Label("Watch demo", systemImage: "play.circle")
                .font(.caption.weight(.medium))
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
        }
        .buttonStyle(.bordered)
        .controlSize(.small)
        .tint(.accentColor)
    }
}
