import Foundation
import Combine

/// Pure state machine for the stretch player — no SwiftUI dependency, so
/// it's directly unit-testable. The view drives `tick()` on a one-second
/// cadence and listens for `finishedEvent` to record the session exactly
/// once.
final class PlayerEngine: ObservableObject {

    struct Snapshot: Equatable {
        var stretches: [Stretch]
        var index: Int
        var remainingSeconds: Int
        var running: Bool
        var finished: Bool

        var current: Stretch? {
            stretches.indices.contains(index) ? stretches[index] : nil
        }

        var progress: Double {
            guard let current, current.durationSeconds > 0 else { return 0 }
            return Double(current.durationSeconds - remainingSeconds) / Double(current.durationSeconds)
        }

        /// Progress across the entire routine, weighted by per-stretch
        /// duration. 0 at first frame, 1 at the moment the last stretch
        /// finishes.
        var routineProgress: Double {
            if finished { return 1 }
            let total = stretches.reduce(0) { $0 + $1.durationSeconds }
            guard total > 0 else { return 0 }
            let elapsedBefore = stretches.prefix(index).reduce(0) { $0 + $1.durationSeconds }
            let elapsedInCurrent = (current?.durationSeconds ?? 0) - remainingSeconds
            return min(1, max(0, Double(elapsedBefore + elapsedInCurrent) / Double(total)))
        }
    }

    /// Set exactly once when the routine completes. Views observe this
    /// (via `.onChange`) to record the session.
    struct FinishedEvent: Equatable {
        let totalDurationSeconds: Int
    }

    @Published private(set) var snapshot: Snapshot
    @Published private(set) var finishedEvent: FinishedEvent?
    let totalDurationSeconds: Int

    /// `startIndex` lets the caller resume an interrupted routine — pass
    /// the saved index and the engine starts at that stretch.
    init(stretches: [Stretch], startIndex: Int = 0) {
        self.totalDurationSeconds = stretches.reduce(0) { $0 + $1.durationSeconds }
        let safe = max(0, min(startIndex, stretches.count - 1))
        let index = stretches.isEmpty ? 0 : safe
        self.snapshot = Snapshot(
            stretches: stretches,
            index: index,
            remainingSeconds: stretches.indices.contains(index)
                ? stretches[index].durationSeconds : 0,
            running: !stretches.isEmpty,
            finished: stretches.isEmpty
        )
    }

    /// Advance the clock by one second. Returns true if this tick caused
    /// the routine to finish. Callers don't need the return value for
    /// session recording — observe `finishedEvent` instead.
    @discardableResult
    func tick() -> Bool {
        guard snapshot.running, !snapshot.finished else { return false }
        if snapshot.remainingSeconds > 1 {
            snapshot.remainingSeconds -= 1
            return false
        } else {
            return advance()
        }
    }

    @discardableResult
    func next() -> Bool {
        guard !snapshot.finished else { return false }
        return advance()
    }

    func previous() {
        let prev = max(0, snapshot.index - 1)
        snapshot.index = prev
        snapshot.remainingSeconds = snapshot.stretches[prev].durationSeconds
        snapshot.finished = false
        snapshot.running = true
    }

    func togglePlay() {
        snapshot.running.toggle()
    }

    private func advance() -> Bool {
        let nextIdx = snapshot.index + 1
        if nextIdx >= snapshot.stretches.count {
            snapshot.finished = true
            snapshot.running = false
            finishedEvent = FinishedEvent(totalDurationSeconds: totalDurationSeconds)
            return true
        } else {
            snapshot.index = nextIdx
            snapshot.remainingSeconds = snapshot.stretches[nextIdx].durationSeconds
            return false
        }
    }
}
