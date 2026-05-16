import Foundation
import Combine

/// Pure state machine for the stretch player — no SwiftUI dependency, so
/// it's directly unit-testable. The view drives `tick()` on a one-second
/// cadence and listens for `snapshot.finished`.
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
    }

    @Published private(set) var snapshot: Snapshot
    let totalDurationSeconds: Int

    init(stretches: [Stretch]) {
        self.totalDurationSeconds = stretches.reduce(0) { $0 + $1.durationSeconds }
        self.snapshot = Snapshot(
            stretches: stretches,
            index: 0,
            remainingSeconds: stretches.first?.durationSeconds ?? 0,
            running: !stretches.isEmpty,
            finished: stretches.isEmpty
        )
    }

    /// Advance the clock by one second. Returns true if this tick caused the
    /// routine to finish.
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
            return true
        } else {
            snapshot.index = nextIdx
            snapshot.remainingSeconds = snapshot.stretches[nextIdx].durationSeconds
            return false
        }
    }
}
