import Foundation
import SwiftData

/// One recorded pain rating. The same row type covers both PRE_SESSION
/// and POST_SESSION ratings — `context` discriminates and `sessionId`
/// is only populated for POST rows so the history screen can pair them
/// to the session that just finished.
@Model
final class PainLog: PainMeasurement {
    var recordedAt: Date
    var painLevel: Int
    var bodyLocationTag: String?
    var context: String
    /// Stable string id for the associated session; nil for PRE rows or
    /// when paired session lookup isn't required.
    var sessionId: String?

    init(
        recordedAt: Date = .now,
        painLevel: Int,
        bodyLocationTag: String? = nil,
        context: String,
        sessionId: String? = nil
    ) {
        self.recordedAt = recordedAt
        self.painLevel = painLevel
        self.bodyLocationTag = bodyLocationTag
        self.context = context
        self.sessionId = sessionId
    }
}
