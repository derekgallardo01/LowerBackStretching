import Foundation

/// Two-valued enum baked into `PainLog.context` so the same table holds
/// both "before" and "after" ratings. Stored as the raw string so
/// existing rows survive a model rename.
enum PainContext {
    static let preSession = "PRE_SESSION"
    static let postSession = "POST_SESSION"
}

/// Pure contract for a recorded pain rating. Lives in the data layer
/// so domain functions can run against test doubles without dragging
/// SwiftData into pure unit tests.
protocol PainMeasurement {
    var recordedAt: Date { get }
    var painLevel: Int { get }
    var bodyLocationTag: String? { get }
    var context: String { get }
}

/// Pairs a POST_SESSION log with the nearest preceding PRE_SESSION log
/// (if one exists within the lookback window). `pre` is nil when the
/// user skipped the pre-session prompt.
struct SessionPainPair {
    let pre: PainMeasurement?
    let post: PainMeasurement
}

/// Numeric view of pain change. `delta = post - pre` (negative means
/// improvement). `delta` is nil when no pre log exists.
struct SessionPainDelta: Equatable {
    let pre: Int?
    let post: Int
    let delta: Int?
}

/// Walks the log list and matches each POST to the most recent PRE
/// that occurred before it within `lookback`. Defaults to one hour,
/// matching Android.
func pairSessionPainLogs(
    _ logs: [PainMeasurement],
    lookback: TimeInterval = 60 * 60
) -> [SessionPainPair] {
    let pres = logs
        .filter { $0.context == PainContext.preSession }
        .sorted { $0.recordedAt < $1.recordedAt }
    let posts = logs
        .filter { $0.context == PainContext.postSession }
        .sorted { $0.recordedAt > $1.recordedAt }

    return posts.map { post in
        let pre = pres.last { pre in
            pre.recordedAt <= post.recordedAt &&
                post.recordedAt.timeIntervalSince(pre.recordedAt) <= lookback
        }
        return SessionPainPair(pre: pre, post: post)
    }
}

func sessionPainDelta(_ pair: SessionPainPair) -> SessionPainDelta {
    let pre = pair.pre?.painLevel
    let post = pair.post.painLevel
    return SessionPainDelta(
        pre: pre,
        post: post,
        delta: pre.map { post - $0 }
    )
}
