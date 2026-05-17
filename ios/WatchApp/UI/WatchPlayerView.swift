import SwiftUI

struct WatchPlayerView: View {
    @StateObject private var engine: WatchPlayerEngine

    init() {
        let routine = WatchContent.loadRoutine()
        _engine = StateObject(wrappedValue: WatchPlayerEngine(stretches: routine.stretches))
    }

    var body: some View {
        Group {
            if engine.snapshot.finished {
                finishedView
            } else if let current = engine.snapshot.current {
                playerView(current: current)
            } else {
                ProgressView()
            }
        }
        .onChange(of: engine.snapshot.index) { oldValue, newValue in
            guard newValue != oldValue, !engine.snapshot.finished else { return }
            WatchHaptics.short()
        }
        .onChange(of: engine.finished) { _, finished in
            if finished { WatchHaptics.finish() }
        }
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                engine.tick()
            }
        }
    }

    @ViewBuilder
    private func playerView(current: WatchStretch) -> some View {
        VStack(spacing: 6) {
            Text(current.name)
                .font(.headline)
                .multilineTextAlignment(.center)
                .lineLimit(2)

            Text("\(engine.snapshot.remainingSeconds)s")
                .font(.system(size: 38, weight: .semibold, design: .rounded))
                .monospacedDigit()

            ProgressView(value: max(0, min(1, Double(engine.snapshot.progress))))
                .tint(.accentColor)

            Text("\(engine.snapshot.index + 1) of \(engine.snapshot.stretches.count)")
                .font(.caption2)
                .foregroundStyle(.secondary)

            HStack(spacing: 10) {
                Button { engine.previous() } label: { Image(systemName: "backward.fill") }
                    .disabled(engine.snapshot.index == 0)
                Button { engine.togglePlay() } label: {
                    Image(systemName: engine.snapshot.running ? "pause.fill" : "play.fill")
                }
                Button { engine.next() } label: { Image(systemName: "forward.fill") }
            }
            .buttonStyle(.bordered)
            .controlSize(.mini)
        }
        .padding(.horizontal, 6)
    }

    private var finishedView: some View {
        VStack(spacing: 8) {
            Text("Nice work.")
                .font(.title3.weight(.semibold))
            Text("Done.")
                .font(.body)
                .foregroundStyle(.secondary)
        }
    }
}
