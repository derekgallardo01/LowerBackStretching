import Foundation

/// A tappable region on the body-diagram silhouette. Each zone maps to
/// a `bodyParts` tag that already appears in stretches.json — tapping
/// the zone surfaces every stretch that includes the tag.
///
/// Coordinates are normalized to the silhouette's 0..1 bounding box,
/// so the same data drives every screen size. The silhouette is drawn
/// as a back-view single figure; left/right limbs share the same row
/// (HAMSTRINGS / CALVES rectangles span both legs).
enum BodyZone: String, CaseIterable, Identifiable {
    case neck
    case upperBack
    case lowerBack
    case hips
    case glutes
    case hamstrings
    case calves

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .neck: return "Neck"
        case .upperBack: return "Upper back"
        case .lowerBack: return "Lower back"
        case .hips: return "Hips"
        case .glutes: return "Glutes"
        case .hamstrings: return "Hamstrings"
        case .calves: return "Calves"
        }
    }

    var bodyPartTag: String {
        switch self {
        case .neck: return "spine"
        case .upperBack: return "upper-back"
        case .lowerBack: return "lower-back"
        case .hips: return "hips"
        case .glutes: return "glutes"
        case .hamstrings: return "hamstrings"
        case .calves: return "calves"
        }
    }

    var rect: NormalizedRect {
        switch self {
        case .neck:       return NormalizedRect(x: 0.42, y: 0.04, w: 0.16, h: 0.06)
        case .upperBack:  return NormalizedRect(x: 0.30, y: 0.16, w: 0.40, h: 0.12)
        case .lowerBack:  return NormalizedRect(x: 0.32, y: 0.32, w: 0.36, h: 0.12)
        case .hips:       return NormalizedRect(x: 0.27, y: 0.44, w: 0.46, h: 0.07)
        case .glutes:     return NormalizedRect(x: 0.30, y: 0.51, w: 0.40, h: 0.10)
        case .hamstrings: return NormalizedRect(x: 0.30, y: 0.63, w: 0.40, h: 0.15)
        case .calves:     return NormalizedRect(x: 0.32, y: 0.80, w: 0.36, h: 0.12)
        }
    }
}

/// Rectangle in [0..1] x [0..1] silhouette space.
struct NormalizedRect: Equatable {
    let x: CGFloat
    let y: CGFloat
    let w: CGFloat
    let h: CGFloat
}

/// Map a stretch's free-form `bodyParts` tags to the structured
/// `BodyZone` set the silhouette knows how to draw. Multiple tags can
/// highlight the same zone ("spine" lights up neck, upper back, and
/// lower back); unknown tags ("core", "groin", "quads") are silently
/// dropped because no zone exists for them yet.
func bodyZones(forTags tags: [String]) -> Set<BodyZone> {
    var zones: Set<BodyZone> = []
    for tag in tags {
        switch tag {
        case "spine":
            zones.insert(.neck)
            zones.insert(.upperBack)
            zones.insert(.lowerBack)
        case "upper-back":
            zones.insert(.upperBack)
        case "lower-back":
            zones.insert(.lowerBack)
        case "hips", "groin":
            zones.insert(.hips)
        case "glutes":
            zones.insert(.glutes)
        case "hamstrings":
            zones.insert(.hamstrings)
        case "calves":
            zones.insert(.calves)
        default:
            break // "core", "quads" — intentionally unmapped.
        }
    }
    return zones
}
