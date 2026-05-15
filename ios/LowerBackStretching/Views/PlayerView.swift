import SwiftUI
import SwiftData

struct PlayerView: View {
    let programId: String
    let dayNumber: Int

    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @State private var index: Int = 0
    @State private var remaining: Int = 0
    @State private var running: Bool = true
    @State private var finished: Bool = false

    private var program: Program? { content.program(id: programId) }
    private var stretches: [Stretch] {
        guard let program else { return [] }
        return content.stretches(for: program, day: dayNumber)
    }
    private var current: Stretch? { stretches[safe: index] }
    private var progress: Double {
        guard let current, current.durationSeconds > 0 else { return 0 }
        return Double(current.durationSeconds - remaining) / Double(current.durationSeconds)
    }

    var body: some View {
        Group {
            if finished {
                FinishedView { dismiss() }
            } else if let current {
                VStack(spacing: 16) {
                    YouTubeView(videoId: current.youtubeId)
                        .aspectRatio(16.0 / 9.0, contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                    Text(current.name).font(.title2.weight(.semibold))
                    Text(current.description).font(.body).foregroundStyle(.secondary)

                    ProgressView(value: progress).tint(.accentColor)
                    Text("\(remaining)s · \(index + 1) of \(stretches.count)")
                        .font(.caption.weight(.medium))

                    HStack(spacing: 24) {
                        Button(action: previous) {
                            Image(systemName: "backward.fill").font(.title)
                        }.disabled(index == 0)

                        Button(running ? "Pause" : "Resume") { running.toggle() }
                            .buttonStyle(.borderedProminent)

                        Button(action: next) {
                            Image(systemName: "forward.fill").font(.title)
                        }
                    }

                    Spacer()
                }
                .padding(16)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Day \(dayNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { if remaining == 0 { remaining = stretches.first?.durationSeconds ?? 0 } }
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard running, !finished else { continue }
                if remaining > 1 {
                    remaining -= 1
                } else {
                    next()
                }
            }
        }
    }

    private func previous() {
        let prev = max(0, index - 1)
        index = prev
        remaining = stretches[prev].durationSeconds
        finished = false
    }

    private func next() {
        let nextIdx = index + 1
        if nextIdx >= stretches.count {
            finished = true
            running = false
            let total = stretches.reduce(0) { $0 + $1.durationSeconds }
            SessionStore.record(programId: programId, day: dayNumber, durationSeconds: total, in: modelContext)
        } else {
            index = nextIdx
            remaining = stretches[nextIdx].durationSeconds
        }
    }
}

private struct FinishedView: View {
    let onDone: () -> Void
    var body: some View {
        VStack(spacing: 16) {
            Text("Nice work.").font(.largeTitle.weight(.semibold))
            Text("Session logged.").font(.body)
            Button("Done", action: onDone).buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

private extension Array {
    subscript(safe i: Int) -> Element? { indices.contains(i) ? self[i] : nil }
}
