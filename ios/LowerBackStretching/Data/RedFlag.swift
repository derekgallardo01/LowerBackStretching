import Foundation

/// One symptom signal in the onboarding safety check. Plain English so
/// it doesn't read like a medical brochure. Order matters — these are
/// listed in the same sequence on Android so screenshots line up.
struct RedFlag: Hashable, Identifiable {
    let text: String
    var id: String { text }
}

/// The five highest-severity signals worth surfacing before a first
/// stretch. More conditions exist (cancer history, unexplained weight
/// loss, night pain) but are harder to phrase non-scarily for an
/// onboarding pager; they can be added later if the bar moves.
enum RedFlagCatalog {
    static let all: [RedFlag] = [
        RedFlag(text: "Numbness or weakness in your legs or feet"),
        RedFlag(text: "Pain shooting down one or both legs, below the knee"),
        RedFlag(text: "Loss of control over your bladder or bowels"),
        RedFlag(text: "Severe back pain after a fall or accident"),
        RedFlag(text: "Fever along with your back pain"),
    ]
}
