import Foundation
import SwiftData

/// Thin write-side wrapper around the PainLog table. Mirrors
/// `PainLogRepository` on Android so the pre/post-session prompt logic
/// matches across platforms.
enum PainLogService {

    /// Insert a PRE_SESSION row at `now`. Returns the row so callers
    /// can carry the id forward if they need to delete on skip.
    @discardableResult
    static func recordPre(
        painLevel: Int,
        bodyLocationTag: String?,
        in context: ModelContext,
        now: Date = .now
    ) -> PainLog {
        let log = PainLog(
            recordedAt: now,
            painLevel: painLevel,
            bodyLocationTag: bodyLocationTag,
            context: PainContext.preSession,
            sessionId: nil
        )
        context.insert(log)
        try? context.save()
        return log
    }

    @discardableResult
    static func recordPost(
        painLevel: Int,
        bodyLocationTag: String?,
        sessionId: String,
        in context: ModelContext,
        now: Date = .now
    ) -> PainLog {
        let log = PainLog(
            recordedAt: now,
            painLevel: painLevel,
            bodyLocationTag: bodyLocationTag,
            context: PainContext.postSession,
            sessionId: sessionId
        )
        context.insert(log)
        try? context.save()
        return log
    }

    static func delete(_ log: PainLog, in context: ModelContext) {
        context.delete(log)
        try? context.save()
    }

    /// True when a PRE_SESSION row exists with `recordedAt >= start-of-day(now)`
    /// in the device's local calendar. Used to gate the pre-session prompt
    /// to once per day.
    static func hasPreLoggedToday(
        logs: [PainLog],
        now: Date = .now,
        calendar: Calendar = .current
    ) -> Bool {
        let start = calendar.startOfDay(for: now)
        return logs.contains { log in
            log.context == PainContext.preSession && log.recordedAt >= start
        }
    }
}
