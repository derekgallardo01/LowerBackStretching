import SwiftUI
import SwiftData

struct PlayerView: View {
    let programId: String
    let dayNumber: Int

    @EnvironmentObject private var content: ContentStore

    var body: some View {
        let stretches: [Stretch] = {
            guard let program = content.program(id: programId) else { return [] }
            return content.stretches(for: program, day: dayNumber)
        }()
        PlayerBody(stretches: stretches, title: "Day \(dayNumber)", programId: programId, dayNumber: dayNumber)
    }
}

struct SinglePlayerView: View {
    let stretchId: String

    @EnvironmentObject private var content: ContentStore

    var body: some View {
        if let stretch = content.stretch(id: stretchId) {
            PlayerBody(
                stretches: [stretch],
                title: stretch.name,
                programId: SyntheticProgramId.single(stretchId),
                dayNumber: 0,
            )
        } else {
            ProgressView()
        }
    }
}

struct CustomRoutinePlayerView: View {
    let routine: CustomRoutine

    @EnvironmentObject private var content: ContentStore

    var body: some View {
        let stretches = routine.stretchIds.compactMap { content.stretch(id: $0) }
        PlayerBody(
            stretches: stretches,
            title: routine.name,
            programId: SyntheticProgramId.routine(routine.id),
            dayNumber: 0,
        )
    }
}

struct PlayerBody: View {
    let title: String
    let programId: String
    let dayNumber: Int

    @StateObject private var engine: PlayerEngine
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    init(stretches: [Stretch], title: String, programId: String, dayNumber: Int) {
        self.title = title
        self.programId = programId
        self.dayNumber = dayNumber
        _engine = StateObject(wrappedValue: PlayerEngine(stretches: stretches))
    }

    var body: some View {
        Group {
            if engine.snapshot.finished {
                FinishedView { dismiss() }
            } else if let current = engine.snapshot.current {
                VStack(spacing: 16) {
                    YouTubeView(videoId: current.youtubeId)
                        .aspectRatio(16.0 / 9.0, contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                    Text(current.name).font(.title2.weight(.semibold))
                    Text(current.description).font(.body).foregroundStyle(.secondary)

                    ProgressView(value: engine.snapshot.progress).tint(.accentColor)
                    Text("\(engine.snapshot.remainingSeconds)s · \(engine.snapshot.index + 1) of \(engine.snapshot.stretches.count)")
                        .font(.caption.weight(.medium))

                    HStack(spacing: 24) {
                        Button(action: { engine.previous() }) {
                            Image(systemName: "backward.fill").font(.title)
                        }.disabled(engine.snapshot.index == 0)

                        Button(engine.snapshot.running ? "Pause" : "Resume") {
                            engine.togglePlay()
                        }
                        .buttonStyle(.borderedProminent)

                        Button(action: skip) {
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
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                let finishedNow = engine.tick()
                if finishedNow { recordSession() }
            }
        }
    }

    private func skip() {
        let finishedNow = engine.next()
        if finishedNow { recordSession() }
    }

    private func recordSession() {
        SessionStore.record(
            programId: programId,
            day: dayNumber,
            durationSeconds: engine.totalDurationSeconds,
            in: modelContext,
        )
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
