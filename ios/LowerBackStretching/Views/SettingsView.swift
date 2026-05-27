import SwiftUI

struct SettingsView: View {
    @AppStorage(SettingsKeys.reminderEnabled) private var enabled: Bool = false
    @AppStorage(SettingsKeys.reminderHour) private var hour: Int = ReminderDefaults.hour
    @AppStorage(SettingsKeys.reminderMinute) private var minute: Int = ReminderDefaults.minute
    @AppStorage(SettingsKeys.themeMode) private var themeModeRaw: String = ThemeMode.system.storageValue
    @AppStorage(SettingsKeys.durationUnit) private var durationUnitRaw: String = DurationUnit.seconds.storageValue
    @AppStorage(SettingsKeys.hapticsTransitions) private var hapticsTransitions: Bool = true
    @AppStorage(SettingsKeys.hapticsFinish) private var hapticsFinish: Bool = true
    @AppStorage(SettingsKeys.musicTrack) private var musicTrackRaw: String = MusicTrack.none.storageValue
    @AppStorage(SettingsKeys.musicVolume) private var musicVolume: Double = Double(AudioDefaults.musicVolume)
    @AppStorage(SettingsKeys.ambientTrack) private var ambientTrackRaw: String = AmbientTrack.none.storageValue
    @AppStorage(SettingsKeys.ambientVolume) private var ambientVolume: Double = Double(AudioDefaults.ambientVolume)
    @AppStorage(SettingsKeys.chimeTrack) private var chimeTrackRaw: String = ChimeTrack.none.storageValue
    @AppStorage(SettingsKeys.healthWriteEnabled) private var healthWriteEnabled: Bool = false
    @AppStorage(SettingsKeys.healthReadEnabled) private var healthReadEnabled: Bool = false
    @AppStorage(SettingsKeys.cloudSyncEnabled) private var cloudSyncEnabled: Bool = false
    @AppStorage(SettingsKeys.streakNudgeEnabled) private var streakNudgeEnabled: Bool = true

    @State private var pickerDate: Date = .now

    var body: some View {
        Form {
            Section(header: Text("Daily reminder")) {
                Toggle("Enabled", isOn: $enabled)
                    .onChange(of: enabled) { _, on in
                        ReminderController.apply(enabled: on, hour: hour, minute: minute)
                    }

                DatePicker("Time", selection: $pickerDate, displayedComponents: .hourAndMinute)
                    .onChange(of: pickerDate) { _, value in
                        let comps = Calendar.current.dateComponents([.hour, .minute], from: value)
                        let h = comps.hour ?? 8
                        let m = comps.minute ?? 0
                        ReminderController.apply(enabled: enabled, hour: h, minute: m)
                    }

                Toggle(isOn: $streakNudgeEnabled) {
                    VStack(alignment: .leading) {
                        Text("Streak safety net")
                        Text("Evening nudge if you'll lose a streak by skipping today.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .onChange(of: streakNudgeEnabled) { _, on in
                    StreakNudgeController.apply(enabled: on)
                }
            }

            Section {
                NavigationLink {
                    SafetyAdvisoryView()
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "cross.case.fill")
                            .foregroundStyle(.tint)
                            .frame(width: 28)
                        VStack(alignment: .leading) {
                            Text("Safety check").font(.body)
                            Text("Symptoms to watch for before stretching")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .accessibilityIdentifier("settingsSafetyCheck")
            }

            Section(header: Text("Appearance")) {
                Picker("Theme", selection: Binding(
                    get: { ThemeMode.fromStorage(themeModeRaw) },
                    set: { themeModeRaw = $0.storageValue },
                )) {
                    ForEach(ThemeMode.allCases, id: \.self) { mode in
                        Text(mode.rawValue.capitalized).tag(mode)
                    }
                }
                .pickerStyle(.segmented)

                Picker("Duration display", selection: Binding(
                    get: { DurationUnit.fromStorage(durationUnitRaw) },
                    set: { durationUnitRaw = $0.storageValue },
                )) {
                    Text("Seconds").tag(DurationUnit.seconds)
                    Text("Minutes").tag(DurationUnit.minutesShort)
                }
                .pickerStyle(.segmented)
            }

            Section(header: Text("Haptics")) {
                Toggle("Stretch transitions", isOn: $hapticsTransitions)
                Toggle("Routine finish", isOn: $hapticsFinish)
            }

            Section(header: Text("Audio")) {
                Picker("Music", selection: Binding(
                    get: { MusicTrack.fromStorage(musicTrackRaw) },
                    set: { musicTrackRaw = $0.storageValue },
                )) {
                    ForEach(MusicTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }
                VStack(alignment: .leading) {
                    Text("Music volume")
                    Slider(value: $musicVolume, in: 0...1)
                }

                Picker("Ambient", selection: Binding(
                    get: { AmbientTrack.fromStorage(ambientTrackRaw) },
                    set: { ambientTrackRaw = $0.storageValue },
                )) {
                    ForEach(AmbientTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }
                VStack(alignment: .leading) {
                    Text("Ambient volume")
                    Slider(value: $ambientVolume, in: 0...1)
                }

                Picker("Chime on transition", selection: Binding(
                    get: { ChimeTrack.fromStorage(chimeTrackRaw) },
                    set: { chimeTrackRaw = $0.storageValue },
                )) {
                    ForEach(ChimeTrack.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }
            }

            Section(header: Text("Apple Health")) {
                if HealthController.shared.isAvailable {
                    Toggle("Write stretching workouts", isOn: $healthWriteEnabled)
                        .onChange(of: healthWriteEnabled) { _, on in
                            if on { HealthController.shared.requestAuthorization { _ in } }
                        }
                    Toggle("Read daily steps", isOn: $healthReadEnabled)
                        .onChange(of: healthReadEnabled) { _, on in
                            if on { HealthController.shared.requestAuthorization { _ in } }
                        }
                } else {
                    Text("Apple Health isn't available on this device.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            Section(header: Text("Cloud sync")) {
                if SyncController.shared.hasRealBackend {
                    Toggle("Enable cloud sync", isOn: $cloudSyncEnabled)
                        .onChange(of: cloudSyncEnabled) { _, on in
                            Task { await SyncController.shared.setEnabled(on) }
                        }
                    Text("Sessions, routines, and progress back up to your account.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                } else {
                    Text("Cloud sync is wired in but the Firebase backend isn't connected yet. Set up the Firebase project (see firebase/README.md) and assign a real backend to SyncController.shared.backend to enable.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                LabeledContent("Backend", value: SyncController.shared.backendType)
            }

            Section(header: Text("About")) {
                LabeledContent("Version", value: "0.1.0")
                LabeledContent("Made with", value: "SwiftUI")
            }
        }
        .navigationTitle("Settings")
        .onAppear { pickerDate = makeTime(hour: hour, minute: minute) }
    }

    private func makeTime(hour: Int, minute: Int) -> Date {
        let comps = DateComponents(hour: hour, minute: minute)
        return Calendar.current.date(from: comps) ?? .now
    }
}
