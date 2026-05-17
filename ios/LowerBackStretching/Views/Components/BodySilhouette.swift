import SwiftUI

/// Back-view body silhouette drawn purely from SwiftUI Shapes (no
/// bundled SVG). Renders in a 1:2 aspect-ratio container so
/// coordinates stay consistent across screen sizes.
///
/// Pass `onZoneTap` for the interactive (find-a-stretch) mode, or
/// pass `highlightedZones` for the read-only (player anatomy overlay)
/// mode. Both can be active simultaneously.
struct BodySilhouette: View {
    var onZoneTap: ((BodyZone) -> Void)? = nil
    var highlightedZones: Set<BodyZone> = []
    var tint: Color = Color.primary.opacity(0.15)
    var highlightColor: Color = .accentColor

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            let h = geo.size.height
            ZStack(alignment: .topLeading) {
                BackSilhouette()
                    .fill(tint)

                ForEach(Array(highlightedZones), id: \.self) { zone in
                    let r = zone.rect
                    RoundedRectangle(cornerRadius: 6)
                        .fill(highlightColor.opacity(0.45))
                        .frame(width: r.w * w, height: r.h * h)
                        .offset(x: r.x * w, y: r.y * h)
                }

                if let onZoneTap {
                    ForEach(BodyZone.allCases) { zone in
                        let r = zone.rect
                        Button(action: { onZoneTap(zone) }) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.clear)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .frame(width: r.w * w, height: r.h * h)
                        .offset(x: r.x * w, y: r.y * h)
                        .accessibilityLabel(zone.displayName)
                    }
                }
            }
        }
        .aspectRatio(0.5, contentMode: .fit)
    }
}

private struct BackSilhouette: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let w = rect.width
        let h = rect.height

        // Head — circle.
        path.addEllipse(in: CGRect(
            x: w * 0.39, y: h * 0.0,
            width: w * 0.22, height: w * 0.22
        ))
        // Neck.
        path.addRoundedRect(
            in: CGRect(x: w * 0.45, y: h * 0.12, width: w * 0.10, height: h * 0.04),
            cornerSize: CGSize(width: w * 0.02, height: w * 0.02)
        )
        // Torso (tapered hexagon).
        var torso = Path()
        torso.move(to: CGPoint(x: w * 0.20, y: h * 0.16))
        torso.addLine(to: CGPoint(x: w * 0.80, y: h * 0.16))
        torso.addLine(to: CGPoint(x: w * 0.72, y: h * 0.45))
        torso.addLine(to: CGPoint(x: w * 0.78, y: h * 0.52))
        torso.addLine(to: CGPoint(x: w * 0.22, y: h * 0.52))
        torso.addLine(to: CGPoint(x: w * 0.28, y: h * 0.45))
        torso.closeSubpath()
        path.addPath(torso)
        // Arms.
        path.addRoundedRect(
            in: CGRect(x: w * 0.04, y: h * 0.17, width: w * 0.13, height: h * 0.30),
            cornerSize: CGSize(width: w * 0.05, height: w * 0.05)
        )
        path.addRoundedRect(
            in: CGRect(x: w * 0.83, y: h * 0.17, width: w * 0.13, height: h * 0.30),
            cornerSize: CGSize(width: w * 0.05, height: w * 0.05)
        )
        // Glutes.
        path.addRoundedRect(
            in: CGRect(x: w * 0.24, y: h * 0.50, width: w * 0.52, height: h * 0.13),
            cornerSize: CGSize(width: w * 0.06, height: w * 0.06)
        )
        // Legs.
        var leftLeg = Path()
        leftLeg.move(to: CGPoint(x: w * 0.26, y: h * 0.60))
        leftLeg.addLine(to: CGPoint(x: w * 0.46, y: h * 0.60))
        leftLeg.addLine(to: CGPoint(x: w * 0.44, y: h * 0.94))
        leftLeg.addLine(to: CGPoint(x: w * 0.30, y: h * 0.94))
        leftLeg.closeSubpath()
        path.addPath(leftLeg)
        var rightLeg = Path()
        rightLeg.move(to: CGPoint(x: w * 0.54, y: h * 0.60))
        rightLeg.addLine(to: CGPoint(x: w * 0.74, y: h * 0.60))
        rightLeg.addLine(to: CGPoint(x: w * 0.70, y: h * 0.94))
        rightLeg.addLine(to: CGPoint(x: w * 0.56, y: h * 0.94))
        rightLeg.closeSubpath()
        path.addPath(rightLeg)

        return path
    }
}
