import SwiftUI
import SwiftData
import UIKit

/// Which pain prompt the player is currently showing. Bound to a
/// `.sheet(item:)` so a single binding switches between pre and post
/// variants. Setting to `nil` dismisses the sheet.
enum PainPromptState: Identifiable, Equatable {
    case preSession
    case postSession(sessionId: String)

    var id: String {
        switch self {
        case .preSession: return "pre"
        case .postSession(let sid): return "post-\(sid)"
        }
    }
}

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
    @Query(sort: \PainLog.recordedAt, order: .reverse) private var painLogs: [PainLog]
    private var unit: DurationUnit { DurationUnit.fromStorage(durationUnitRaw) }

    @State private var show3D: Bool = false
    @State private var painPrompt: PainPromptState? = nil
    @State private var preSessionDecided: Bool = false

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
                    ZStack(alignment: .topTrailing) {
                        Group {
                            if show3D {
                                StretchAnimation3DView(
                                    animation: current.animation,
                                    youtubeId: current.youtubeId
                                )
                            } else {
                                StretchAnimationView(
                                    animation: current.animation,
                                    youtubeId: current.youtubeId
                                )
                            }
                        }
                        .aspectRatio(16.0 / 9.0, contentMode: .fit)

                        Button(action: { show3D.toggle() }) {
                            Text(show3D ? "2D" : "3D")
                                .font(.caption.weight(.bold))
                                .frame(width: 32, height: 32)
                                .background(Color(.systemBackground).opacity(0.85))
                                .clipShape(Circle())
                        }
                        .padding(8)
                        .accessibilityIdentifier("playerToggle3D")
                    }

                    HStack(alignment: .top, spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(current.name).font(.title2.weight(.semibold))
                            Text(current.description).font(.body).foregroundStyle(.secondary)
                        }
                        Spacer(minLength: 0)
                        let zones = bodyZones(forTags: current.bodyParts)
                        if !zones.isEmpty {
                            BodySilhouette(highlightedZones: zones)
                                .frame(width: 50)
                        }
                    }

                    if let feel = current.whatYouShouldFeel {
                        WhatYouShouldFeelOverlay(text: feel)
                    }

                    ProgressView(value: engine.snapshot.progress).tint(.accentColor)
                    Text("\(formatDuration(engine.snapshot.remainingSeconds, unit: unit)) · \(engine.snapshot.index + 1) of \(engine.snapshot.stretches.count)")
                        .font(.caption.weight(.medium))
                    ProgressView(value: engine.snapshot.routineProgress)
                        .tint(.secondary)
                        .scaleEffect(x: 1, y: 0.5, anchor: .center)

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

                    Button(action: skip) {
                        Label(
                            engine.snapshot.index == engine.snapshot.stretches.count - 1
                                ? "Finish routine"
                                : "Mark stretch complete",
                            systemImage: "checkmark"
                        )
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.secondary)
                    .accessibilityIdentifier("playerMarkComplete")

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
            // Prompt for the post-session pain rating. Synthetic session id
            // lets the row later be cross-referenced if we add a per-session
            // history view.
            painPrompt = .postSession(sessionId: "\(programId)#\(dayNumber)#\(Int(Date.now.timeIntervalSince1970))")
        }
        .sheet(item: $painPrompt) { state in
            switch state {
            case .preSession:
                PainCheckInSheet(
                    title: "How's your back right now?",
                    onSubmit: { level, tag in
                        PainLogService.recordPre(painLevel: level, bodyLocationTag: tag, in: modelContext)
                        resumeAfterPrePrompt()
                    },
                    onSkip: { resumeAfterPrePrompt() }
                )
                .interactiveDismissDisabled(true)
            case .postSession(let sessionId):
                PainCheckInSheet(
                    title: "How does it feel now?",
                    onSubmit: { level, tag in
                        PainLogService.recordPost(
                            painLevel: level,
                            bodyLocationTag: tag,
                            sessionId: sessionId,
                            in: modelContext
                        )
                    },
                    onSkip: {}
                )
            }
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
            maybePromptPreSession()
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

    /// Show the pre-session pain prompt at most once per device-local day.
    /// Pauses the engine while the sheet is up so the timer doesn't drain
    /// behind the modal.
    private func maybePromptPreSession() {
        guard !preSessionDecided else { return }
        preSessionDecided = true
        if PainLogService.hasPreLoggedToday(logs: painLogs) { return }
        if engine.snapshot.running { engine.togglePlay() }
        painPrompt = .preSession
    }

    private func resumeAfterPrePrompt() {
        if !engine.snapshot.running && !engine.snapshot.finished {
            engine.togglePlay()
        }
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

private struct WhatYouShouldFeelOverlay: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: "leaf.fill")
                .foregroundStyle(Color.accentColor)
            VStack(alignment: .leading, spacing: 2) {
                Text("What you should feel")
                    .font(.caption.weight(.semibold))
                Text(text).font(.subheadline)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
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
