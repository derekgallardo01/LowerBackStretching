import Foundation
import Combine

/// Pure state machine for the watch-side stretch player. Mirrors the
/// iPhone-side `PlayerEngine` line-for-line; if drift becomes painful,
/// extract both into a shared Swift Package.
final class WatchPlayerEngine: ObservableObject {

    struct Snapshot: Equatable {
        let stretches: [WatchStretch]
        let index: Int
        let remainingSeconds: Int
        let running: Bool
        let finished: Bool

        var current: WatchStretch? { stretches.indices.contains(index) ? stretches[index] : nil }

        var progress: Float {
            guard let c = current, c.durationSeconds > 0 else { return 0 }
            return Float(c.durationSeconds - remainingSeconds) / Float(c.durationSeconds)
        }

        var routineProgress: Float {
            if finished { return 1 }
            let total = stretches.reduce(0) { $0 + $1.durationSeconds }
            guard total > 0 else { return 0 }
            let elapsedBefore = stretches.prefix(index).reduce(0) { $0 + $1.durationSeconds }
            let elapsedInCurrent = (current?.durationSeconds ?? 0) - remainingSeconds
            return min(1, max(0, Float(elapsedBefore + elapsedInCurrent) / Float(total)))
        }
    }

    @Published private(set) var snapshot: Snapshot

    /// Set true once when the routine finishes; the view watches this
    /// via `.onChange(of:)` and fires the long-haptic.
    @Published private(set) var finished: Bool = false

    init(stretches: [WatchStretch]) {
        snapshot = Snapshot(
            stretches: stretches,
            index: 0,
            remainingSeconds: stretches.first?.durationSeconds ?? 0,
            running: !stretches.isEmpty,
            finished: stretches.isEmpty,
        )
        if stretches.isEmpty { finished = true }
    }

    /// Returns true if this tick caused the routine to finish.
    @discardableResult
    func tick() -> Bool {
        guard snapshot.running, !snapshot.finished else { return false }
        if snapshot.remainingSeconds > 1 {
            snapshot = Snapshot(
                stretches: snapshot.stretches,
                index: snapshot.index,
                remainingSeconds: snapshot.remainingSeconds - 1,
                running: snapshot.running,
                finished: snapshot.finished,
            )
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
        snapshot = Snapshot(
            stretches: snapshot.stretches,
            index: prev,
            remainingSeconds: snapshot.stretches[prev].durationSeconds,
            running: true,
            finished: false,
        )
    }

    func togglePlay() {
        snapshot = Snapshot(
            stretches: snapshot.stretches,
            index: snapshot.index,
            remainingSeconds: snapshot.remainingSeconds,
            running: !snapshot.running,
            finished: snapshot.finished,
        )
    }

    @discardableResult
    private func advance() -> Bool {
        let nextIdx = snapshot.index + 1
        if nextIdx >= snapshot.stretches.count {
            snapshot = Snapshot(
                stretches: snapshot.stretches,
                index: snapshot.index,
                remainingSeconds: snapshot.remainingSeconds,
                running: false,
                finished: true,
            )
            finished = true
            return true
        } else {
            snapshot = Snapshot(
                stretches: snapshot.stretches,
                index: nextIdx,
                remainingSeconds: snapshot.stretches[nextIdx].durationSeconds,
                running: snapshot.running,
                finished: false,
            )
            return false
        }
    }
}
