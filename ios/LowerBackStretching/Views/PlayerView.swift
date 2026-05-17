import SwiftUI
import SwiftData
import UIKit

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
                dayNumber: 0
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
            dayNumber: 0
        )
    }
}

struct PlayerBody: View {
    let title: String
    let programId: String
    let dayNumber: Int

    @StateObject private var engine: PlayerEngine
    @EnvironmentObject private var content: ContentStore
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @AppStorage(SettingsKeys.hapticsTransitions) private var hapticsTransitions: Bool = true
    @AppStorage(SettingsKeys.hapticsFinish) private var hapticsFinish: Bool = true
    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    @AppStorage(SettingsKeys.musicTrack) private var musicTrackRaw: String = MusicTrack.none.storageValue
    @AppStorage(SettingsKeys.musicVolume) private var musicVolume: Double = Double(AudioDefaults.musicVolume)
    @AppStorage(SettingsKeys.ambientTrack) private var ambientTrackRaw: String = AmbientTrack.none.storageValue
    @AppStorage(SettingsKeys.ambientVolume) private var ambientVolume: Double = Double(AudioDefaults.ambientVolume)
    @AppStorage(SettingsKeys.chimeTrack) private var chimeTrackRaw: String = ChimeTrack.none.storageValue
    @AppStorage(SettingsKeys.healthWriteEnabled) private var healthWriteEnabled: Bool = false
    private var unit: DurationUnit { DurationUnit.fromStorage(durationUnitRaw) }

    init(stretches: [Stretch], title: String, programId: String, dayNumber: Int) {
        self.title = title
        self.programId = programId
        self.dayNumber = dayNumber
        let startIndex = InProgressStore.resumeIndex(for: programId, dayNumber: dayNumber)
        _engine = StateObject(
            wrappedValue: PlayerEngine(stretches: stretches, startIndex: startIndex)
        )
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
                    Text("\(formatDuration(engine.snapshot.remainingSeconds, unit: unit)) · \(engine.snapshot.index + 1) of \(engine.snapshot.stretches.count)")
                        .font(.caption.weight(.medium))

                    HStack(spacing: 24) {
                        Button(action: { engine.previous() }) {
                            Image(systemName: "backward.fill").font(.title)
                        }
                        .accessibilityIdentifier("playerPrevious")
                        .disabled(engine.snapshot.index == 0)

                        Button(engine.snapshot.running ? "Pause" : "Resume") {
                            engine.togglePlay()
                        }
                        .buttonStyle(.borderedProminent)
                        .accessibilityIdentifier("playerPause")

                        Button(action: skip) {
                            Image(systemName: "forward.fill").font(.title)
                        }
                        .accessibilityIdentifier("playerNext")
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
        .onChange(of: engine.finishedEvent) { _, event in
            guard let event else { return }
            SessionStore.record(
                programId: programId,
                day: dayNumber,
                durationSeconds: event.totalDurationSeconds,
                in: modelContext
            )
            if let program = content.program(id: programId) {
                ProgramProgressService.advance(
                    programId: programId,
                    completedDay: dayNumber,
                    totalDays: program.days.count,
                    in: modelContext
                )
            }
            if healthWriteEnabled {
                let end = Date.now
                let start = end.addingTimeInterval(-Double(event.totalDurationSeconds))
                HealthController.shared.writeStretchingWorkout(start: start, end: end) { _ in }
            }
            InProgressStore.clear()
            if hapticsFinish { Haptics.finish() }
        }
        .onChange(of: engine.snapshot.index) { oldValue, newValue in
            guard newValue != oldValue, !engine.snapshot.finished else { return }
            InProgressStore.save(
                InProgressSession(programId: programId, dayNumber: dayNumber, index: newValue)
            )
            if hapticsTransitions { Haptics.short() }
            AudioController.shared.playChime(ChimeTrack.fromStorage(chimeTrackRaw))
        }
        .onChange(of: musicTrackRaw) { _, _ in applyMusic() }
        .onChange(of: musicVolume) { _, _ in applyMusic() }
        .onChange(of: ambientTrackRaw) { _, _ in applyAmbient() }
        .onChange(of: ambientVolume) { _, _ in applyAmbient() }
        .onAppear {
            UIApplication.shared.isIdleTimerDisabled = true
            // Persist resume point on first frame too (covers force-kill
            // before any index advance).
            InProgressStore.save(
                InProgressSession(programId: programId, dayNumber: dayNumber, index: engine.snapshot.index)
            )
            applyMusic()
            applyAmbient()
        }
        .onDisappear {
            UIApplication.shared.isIdleTimerDisabled = false
            AudioController.shared.stopAll()
        }
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                engine.tick()
            }
        }
    }

    private func skip() {
        engine.next()
    }

    private func applyMusic() {
        AudioController.shared.setMusic(
            MusicTrack.fromStorage(musicTrackRaw),
            volume: Float(musicVolume)
        )
    }

    private func applyAmbient() {
        AudioController.shared.setAmbient(
            AmbientTrack.fromStorage(ambientTrackRaw),
            volume: Float(ambientVolume)
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
